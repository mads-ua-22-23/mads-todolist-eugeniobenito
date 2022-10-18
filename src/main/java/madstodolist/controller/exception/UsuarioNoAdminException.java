package madstodolist.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason="Usuario sin permisos de acceso")
public class UsuarioNoAdminException extends RuntimeException {
}
