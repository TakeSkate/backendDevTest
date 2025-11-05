package es.jgarci.similarproducts.domain.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void notFoundMapsTo404Problem() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ProblemDetail pd = handler.handleNotFound(new NotFoundException("Product 99 not found"));

        assertThat(pd.getStatus()).isEqualTo(404);
        assertThat(pd.getDetail()).isEqualTo("Product 99 not found");
    }
}
