package com.example.templatespring.dto.produto.response;

import com.example.templatespring.dto.produto.request.ItemCatalogoProdutoRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CatalogoFonteResponse {

    private String origem;
    private String versao;
    private List<ItemCatalogoProdutoRequest> itens = new ArrayList<>();
}
