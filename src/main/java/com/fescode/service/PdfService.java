package com.fescode.service;

import com.fescode.dto.response.PedidoPdfDTO;

public interface PdfService {
    byte[] generarBoletaDesdeDto(PedidoPdfDTO pedido) throws Exception;
}
