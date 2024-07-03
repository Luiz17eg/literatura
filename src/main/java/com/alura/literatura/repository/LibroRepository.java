package com.alura.literatura.repository;

import com.alura.literatura.model.Idioma;
import com.alura.literatura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByLenguaje(Idioma idioma);

    Optional<Libro> findByTitulo(String titulo);

    @Query("SELECT l FROM Libro l ORDER BY l.numero_descargas DESC LIMIT 5")
    List<Libro> top5LibrosMasDescargados();

    List<Libro> findByAutor_Nombre(String nombreAutor);
}
