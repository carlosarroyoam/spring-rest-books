package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.core.pagination.PagedResponse;
import com.carlosarroyoam.rest.books.payment.dto.CreatePaymentRequest;
import com.carlosarroyoam.rest.books.payment.dto.PaymentResponse;
import com.carlosarroyoam.rest.books.payment.dto.PaymentSpecs;
import com.carlosarroyoam.rest.books.payment.dto.UpdatePaymentStatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Expone las operaciones REST sobre {@link com.carlosarroyoam.rest.books.payment.entity.Payment}:
 * listado paginado con filtros, consulta por id, registro y actualización de estado. Todas las
 * operaciones requieren rol {@code App/Admin}.
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {
  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  /**
   * Lista pagos de forma paginada, aplicando los filtros de {@link PaymentSpecs}.
   *
   * @param paymentSpecs filtros opcionales de búsqueda
   * @param pageable configuración de página y orden
   * @return la página de pagos encontrados
   */
  @GetMapping(produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<PagedResponse<PaymentResponse>> findAll(
      @Valid @ModelAttribute PaymentSpecs paymentSpecs,
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<PaymentResponse> payments = paymentService.findAll(paymentSpecs, pageable);
    return ResponseEntity.ok(payments);
  }

  /**
   * Busca un pago por su id.
   *
   * @param paymentId id del pago a buscar
   * @return el pago encontrado
   */
  @GetMapping(value = "/{paymentId}", produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<PaymentResponse> findById(@PathVariable Long paymentId) {
    PaymentResponse paymentById = paymentService.findById(paymentId);
    return ResponseEntity.ok(paymentById);
  }

  /**
   * Registra el pago de una orden.
   *
   * @param request datos del pago a registrar
   * @param builder utilizado para construir la URI del recurso creado
   * @return respuesta 201 con la cabecera {@code Location} del pago creado
   */
  @PostMapping(consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> create(
      @Valid @RequestBody CreatePaymentRequest request, UriComponentsBuilder builder) {
    PaymentResponse createdPayment = paymentService.create(request);
    UriComponents uriComponents =
        builder.path("/payments/{paymentId}").buildAndExpand(createdPayment.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  /**
   * Actualiza el estado de un pago.
   *
   * @param paymentId id del pago a actualizar
   * @param request nuevo estado del pago
   */
  @PutMapping(value = "/{paymentId}/status", consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> updateStatus(
      @PathVariable Long paymentId, @Valid @RequestBody UpdatePaymentStatusRequest request) {
    paymentService.updateStatus(paymentId, request);
    return ResponseEntity.noContent().build();
  }
}
