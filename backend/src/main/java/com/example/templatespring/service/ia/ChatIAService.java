package com.example.templatespring.service.ia;

import com.example.templatespring.dto.ia.ChatRequest;
import com.example.templatespring.dto.ia.ChatResponse;
import com.example.templatespring.dto.ia.IntencaoEnum;
import com.example.templatespring.dto.simulacao.request.SimulacaoRequest;
import com.example.templatespring.dto.simulacao.response.CenarioResponse;
import com.example.templatespring.dto.simulacao.response.SimulacaoResponse;
import com.example.templatespring.entity.enums.PerfilInvestidor;
import com.example.templatespring.entity.enums.OrigemSimulacao;
import com.example.templatespring.exception.IAIndisponivelException;
import com.example.templatespring.service.SimulacaoService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatIAService {

    private static final String DISCLAIMER =
            "As respostas são informativas e não constituem recomendação ou decisão de investimento.";
    private static final List<String> SUGESTOES_PADRAO = List.of(
            "Explique o resultado desta simulação",
            "O que muda se eu aumentar o prazo?",
            "Compare risco, liquidez e rentabilidade");
    private static final Pattern FORA_ESCOPO = Pattern.compile(
            "\\b(futebol|basquete|nba|clima|chuva|temperatura|doenca|diagnostico|receita culinaria|filme|serie|musica|celebridade)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PRAZO = Pattern.compile(
            "(?:prazo|por|em|para)\\s*(?:de|para)?\\s*(\\d{1,3})\\s*(mes(?:es)?|ano(?:s)?)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern APORTE = Pattern.compile(
            "aporte(?:\\s+mensal)?\\s*(?:de|para|:)?\\s*(?:r\\$\\s*)?([\\d.,]+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern VALOR_INICIAL = Pattern.compile(
            "(?:valor\\s+inicial|aporte\\s+inicial|investir|investimento)\\s*(?:de|para|com|:)?\\s*(?:r\\$\\s*)?([\\d.,]+)",
            Pattern.CASE_INSENSITIVE);

    private final IAService iaService;
    private final SimulacaoService simulacaoService;

    public ChatIAService(IAService iaService, SimulacaoService simulacaoService) {
        this.iaService = iaService;
        this.simulacaoService = simulacaoService;
    }

    public ChatResponse processar(ChatRequest request) {
        String mensagem = request.getMensagem().trim();
        if (FORA_ESCOPO.matcher(normalizar(mensagem)).find()) {
            return respostaForaDoEscopo();
        }

        PerfilInvestidor perfil = request.getPerfil() == null
                ? PerfilInvestidor.CONSERVADOR
                : request.getPerfil();

        try {
            if (request.getSimulacaoIdAtual() != null) {
                SimulacaoResponse atual = simulacaoService.buscarPorId(request.getSimulacaoIdAtual());
                Refinamento refinamento = extrairRefinamento(request, mensagem, atual);

                if (refinamento.alterado()) {
                    SimulacaoResponse atualizada = simularNovamente(atual, refinamento, perfil);
                    return respostaDaSimulacaoAtualizada(atualizada, refinamento);
                }

                return respostaComIA(
                        IntencaoEnum.EXPLICAR,
                        atual,
                        promptSobreSimulacao(mensagem, atual, perfil));
            }

            return respostaComIA(
                    IntencaoEnum.EXPLICAR,
                    null,
                    promptSemSimulacao(mensagem, perfil));
        } catch (IAIndisponivelException exception) {
            log.warn("Assistente indisponível: {}", exception.getMessage());
            return fallback(null, exception.getMessage());
        }
    }

    private ChatResponse respostaComIA(
            IntencaoEnum intencao,
            SimulacaoResponse simulacao,
            String prompt) {
        try {
            return ChatResponse.builder()
                    .resposta(iaService.responder(prompt) + "\n\n" + DISCLAIMER)
                    .intencao(intencao)
                    .simulacaoAtualizada(simulacao)
                    .fallback(false)
                    .sugestoes(SUGESTOES_PADRAO)
                    .build();
        } catch (IAIndisponivelException exception) {
            return fallback(simulacao, exception.getMessage());
        }
    }

    private ChatResponse respostaDaSimulacaoAtualizada(
            SimulacaoResponse atualizada,
            Refinamento refinamento) {
        String texto = "Atualizei a simulação para "
                + refinamento.prazoMeses() + " meses, com valor inicial de R$ "
                + refinamento.valorInicial().toPlainString() + " e aporte mensal de R$ "
                + refinamento.aporteMensal().toPlainString() + ".";
        try {
            String explicacao = iaService.responder(promptSobreSimulacao(texto, atualizada, refinamento.perfil()));
            texto += "\n\n" + explicacao;
        } catch (IAIndisponivelException exception) {
            texto += "\n\nO cálculo foi atualizado, mas a explicação automática está indisponível no momento.";
            return ChatResponse.builder()
                    .resposta(texto + "\n\n" + DISCLAIMER)
                    .intencao(IntencaoEnum.SIMULAR)
                    .simulacaoAtualizada(atualizada)
                    .fallback(true)
                    .sugestoes(SUGESTOES_PADRAO)
                    .build();
        }
        return ChatResponse.builder()
                .resposta(texto + "\n\n" + DISCLAIMER)
                .intencao(IntencaoEnum.SIMULAR)
                .simulacaoAtualizada(atualizada)
                .fallback(false)
                .sugestoes(SUGESTOES_PADRAO)
                .build();
    }

    private SimulacaoResponse simularNovamente(
            SimulacaoResponse atual,
            Refinamento refinamento,
            PerfilInvestidor perfil) {
        SimulacaoRequest request = new SimulacaoRequest();
        request.setValorInicial(refinamento.valorInicial());
        request.setAporteMensal(refinamento.aporteMensal());
        request.setPrazoMeses(refinamento.prazoMeses());
        request.setPerfil(perfil);
        request.setProdutoIds(atual.getCenarios().stream().map(CenarioResponse::getProdutoId).toList());
        return simulacaoService.simularComOrigem(request, OrigemSimulacao.ASSISTENTE_IA);
    }

    private Refinamento extrairRefinamento(ChatRequest request, String mensagem, SimulacaoResponse atual) {
        CenarioResponse primeiro = atual.getCenarios().get(0);
        BigDecimal valorInicial = request.getValorInicialRefinamento() != null
                ? request.getValorInicialRefinamento()
                : extrairValor(VALOR_INICIAL, mensagem, primeiro.getValorInicial());
        BigDecimal aporteMensal = request.getAporteMensalRefinamento() != null
                ? request.getAporteMensalRefinamento()
                : extrairValor(APORTE, mensagem, primeiro.getAporteMensal());
        int prazoMeses = request.getPrazoMesesRefinamento() != null
                ? request.getPrazoMesesRefinamento()
                : extrairPrazo(mensagem, primeiro.getEvolucaoMensal().size());

        boolean alterado = request.getValorInicialRefinamento() != null
                || request.getAporteMensalRefinamento() != null
                || request.getPrazoMesesRefinamento() != null
                || valorInicial.compareTo(primeiro.getValorInicial()) != 0
                || aporteMensal.compareTo(primeiro.getAporteMensal()) != 0
                || prazoMeses != primeiro.getEvolucaoMensal().size();

        return new Refinamento(valorInicial, aporteMensal, prazoMeses,
                request.getPerfil() == null ? PerfilInvestidor.CONSERVADOR : request.getPerfil(), alterado);
    }

    private BigDecimal extrairValor(Pattern padrao, String mensagem, BigDecimal padraoValor) {
        Matcher matcher = padrao.matcher(normalizar(mensagem));
        return matcher.find() ? converterNumero(matcher.group(1), padraoValor) : padraoValor;
    }

    private int extrairPrazo(String mensagem, int padraoPrazo) {
        Matcher matcher = PRAZO.matcher(normalizar(mensagem));
        if (!matcher.find()) {
            return padraoPrazo;
        }
        int valor = Integer.parseInt(matcher.group(1));
        return matcher.group(2).startsWith("ano") ? valor * 12 : valor;
    }

    private BigDecimal converterNumero(String valor, BigDecimal padrao) {
        try {
            String normalizado = valor;
            if (valor.contains(",")) {
                normalizado = valor.replace(".", "").replace(",", ".");
            } else if (valor.indexOf('.') != valor.lastIndexOf('.')) {
                normalizado = valor.replace(".", "");
            }
            return new BigDecimal(normalizado);
        } catch (NumberFormatException exception) {
            return padrao;
        }
    }

    private String promptSobreSimulacao(String mensagem, SimulacaoResponse simulacao, PerfilInvestidor perfil) {
        StringBuilder contexto = new StringBuilder();
        for (CenarioResponse cenario : simulacao.getCenarios()) {
            contexto.append("Produto: ").append(cenario.getNomeProduto())
                    .append("; valor líquido: R$ ").append(cenario.getValorFinalLiquido())
                    .append("; rentabilidade: ").append(cenario.getRentabilidade()).append("%")
                    .append("; liquidez: ").append(cenario.getLiquidez())
                    .append("; risco: ").append(cenario.getNivelRisco()).append("\n");
        }
        return """
                Perfil do investidor: %s
                Dados da simulação atual:
                %s
                Origem dos dados: %s. Tipo: %s. Data de referência: %s.
                Pergunta ou pedido do usuário: %s

                Explique os fatores considerados e os limites do resultado. Cite somente os produtos listados.
                Se o usuário pedir alteração de prazo ou valores, descreva o impacto sem executar contratação.
                """.formatted(perfil, contexto, simulacao.getFonteDados(), simulacao.getTipoDado(),
                simulacao.getDataReferencia(), mensagem);
    }

    private String promptSemSimulacao(String mensagem, PerfilInvestidor perfil) {
        return """
                Perfil informado: %s
                Mensagem do usuário: %s

                Responda apenas se o assunto for investimento, produto financeiro ou simulação. Explique conceitos
                com clareza e informe que uma comparação numérica exige selecionar produtos e executar uma simulação.
                Não recomende uma contratação específica e não invente dados do catálogo.
                """.formatted(perfil, mensagem);
    }

    private ChatResponse respostaForaDoEscopo() {
        return ChatResponse.builder()
                .resposta("Posso ajudar apenas com simulações, produtos financeiros, prazos, aportes, risco, liquidez e rentabilidade. "
                        + "Não consigo responder a esse assunto.\n\n" + DISCLAIMER)
                .intencao(IntencaoEnum.DESCONHECIDA)
                .fallback(true)
                .sugestoes(SUGESTOES_PADRAO)
                .build();
    }

    private ChatResponse fallback(SimulacaoResponse simulacao, String motivo) {
        return ChatResponse.builder()
                .resposta("Não consegui consultar o assistente agora. "
                        + "A simulação calculada pelo sistema continua disponível; tente novamente mais tarde.\n\n"
                        + DISCLAIMER)
                .intencao(IntencaoEnum.EXPLICAR)
                .simulacaoAtualizada(simulacao)
                .fallback(true)
                .sugestoes(SUGESTOES_PADRAO)
                .build();
    }

    private String normalizar(String texto) {
        return texto.toLowerCase(Locale.ROOT).replace("á", "a").replace("à", "a")
                .replace("ã", "a").replace("â", "a").replace("é", "e").replace("ê", "e")
                .replace("í", "i").replace("ó", "o").replace("ô", "o").replace("õ", "o")
                .replace("ú", "u").replace("ç", "c");
    }

    private record Refinamento(
            BigDecimal valorInicial,
            BigDecimal aporteMensal,
            int prazoMeses,
            PerfilInvestidor perfil,
            boolean alterado) {}
}
