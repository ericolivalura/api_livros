package com.one.Literalura.repository;

import com.one.Literalura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    Autor findByNome(String nombre);
    List<Autor> findByAnoDeNascimentoLessThanEqualAndAnoDeFalecimentoGreaterThan(Integer anoDeNascimento, Integer anoDeFalecimento);

}