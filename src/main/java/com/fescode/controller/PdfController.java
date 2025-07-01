package com.fescode.controller;

import com.fescode.dto.response.PedidoPdfDTO;
import com.fescode.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // Permitir solicitudes desde el frontend
@RequestMapping("/api/facturacion")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;
    private final RestTemplate restTemplate;

    @GetMapping("/boleta/{pedidoId}")
    public ResponseEntity<byte[]> generarBoleta(@PathVariable Long pedidoId,@RequestHeader("Authorization") String token) {
        try {
            String url = "http://localhost:8080/api/pdf/boleta/datos/" + pedidoId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token); // enviamos token de autorizacion al monolito

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<PedidoPdfDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                PedidoPdfDTO.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return ResponseEntity.status(response.getStatusCode()).build();
            }

            byte[] pdfBytes = pdfService.generarBoletaDesdeDto(response.getBody());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=boleta_" + pedidoId + ".pdf")
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
