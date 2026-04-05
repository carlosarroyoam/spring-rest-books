package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.payment.dto.CreatePaymentRequestDto;
import com.carlosarroyoam.rest.books.payment.dto.PaymentDto;
import com.carlosarroyoam.rest.books.payment.dto.UpdatePaymentStatusRequestDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/payments")
public class PaymentController {
  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @GetMapping(produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<PagedResponseDto<PaymentDto>> findAll(
      @PageableDefault(page = 0, size = 25) Pageable pageable) {
    PagedResponseDto<PaymentDto> payments = paymentService.findAll(pageable);
    return ResponseEntity.ok(payments);
  }

  @GetMapping(value = "/{paymentId}", produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<PaymentDto> findById(@PathVariable Long paymentId) {
    PaymentDto paymentById = paymentService.findById(paymentId);
    return ResponseEntity.ok(paymentById);
  }

  @PostMapping(consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> create(@Valid @RequestBody CreatePaymentRequestDto requestDto,
      UriComponentsBuilder builder) {
    PaymentDto createdPayment = paymentService.create(requestDto);
    UriComponents uriComponents = builder.path("/payments/{paymentId}")
        .buildAndExpand(createdPayment.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  @PutMapping(value = "/{paymentId}/status", consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> updateStatus(@PathVariable Long paymentId,
      @Valid @RequestBody UpdatePaymentStatusRequestDto requestDto) {
    paymentService.updateStatus(paymentId, requestDto);
    return ResponseEntity.noContent().build();
  }
}
