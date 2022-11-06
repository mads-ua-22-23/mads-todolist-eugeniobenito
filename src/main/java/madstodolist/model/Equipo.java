package madstodolist.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "equipos")
public class Equipo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String nombre;

    public Equipo(String nombre) {
        this.nombre = nombre;
    }    

    public String getNombre() {
        return this.nombre;
    }

    public Long getId() {
        return id;
    }
}
