package madstodolist.service;

import madstodolist.model.Equipo;
import madstodolist.model.EquipoRepository;
import madstodolist.model.Usuario;
import madstodolist.model.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipoService {
    Logger logger = LoggerFactory.getLogger(EquipoService.class);

    @Autowired
    EquipoRepository equipoRepository;
    @Autowired
    UsuarioRepository usuarioRepository;

    @Transactional
    public Equipo crearEquipo(String nombre) {
        if (nombre.equals(""))
            throw new EquipoServiceException("El nombre del equipo no puede estar vacío");

        logger.debug("Creando equipo " + nombre);
        Equipo equipo = new Equipo(nombre);
        equipoRepository.save(equipo);
        return equipo; 
    }

    @Transactional(readOnly = true)
    public Equipo recuperarEquipo(Long id) {
        logger.debug("Devolviendo el equipo con id: " + id);
        return equipoRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Equipo> findAllOrderedByName() {
        logger.debug("Devolviendo el listado de equipos");
        return equipoRepository.findAllByOrderByNombreAsc();
    }

    @Transactional
    public void addUsuarioEquipo(Long usuario_id, Long equipo_id) {
        logger.debug("Añadiendo el usuario " + usuario_id + " al equipo " + equipo_id);
        Equipo equipo = equipoRepository.findById(equipo_id).orElse(null);
        Usuario usuario = usuarioRepository.findById(usuario_id).orElse(null);;
        equipo.addUsuario(usuario);
        equipoRepository.save(equipo);
    }

    @Transactional
    public void removeUsuarioEquipo(Long usuario_id, Long equipo_id) {
        logger.debug("Eliminando el usuario " + usuario_id + " del equipo " + equipo_id);
        Equipo equipo = equipoRepository.findById(equipo_id).orElse(null);
        Usuario usuario = usuarioRepository.findById(usuario_id).orElse(null);;
        equipo.removeUsuario(usuario);
        equipoRepository.save(equipo);
    }

    @Transactional(readOnly = true)
    public List<Usuario> usuariosEquipo(Long equipo_id) {
        logger.debug("Devolviendo el listado de usuarios del equipo " + equipo_id);
        Equipo equipo = equipoRepository.findById(equipo_id).orElse(null);
        List <Usuario> usuarios = new ArrayList(equipo.getUsuarios());
        return usuarios;
    }
}
