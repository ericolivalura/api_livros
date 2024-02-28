package com.one.Literalura.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "livros")
public class Livro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String titulo;
    @ManyToOne()
    private Autor autor;
    private List<String> idiomas;
    private Double numeroDeDownloads;

    public Livro() {
    }

    public Livro(DadosLivro dadosLivro, Autor autor) {
        this.titulo = dadosLivro.titulo();
        this.autor = autor;
        this.idiomas = dadosLivro.idiomas();
        this.numeroDeDownloads = dadosLivro.numeroDeDownloads();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public List<String> getIdiomas() {
        return idiomas;
    }

    public Double getNumeroDeDownloads() {
        return numeroDeDownloads;
    }

    @Override
    public String toString() {
        return "----- LIBRO -----" +
                "\n Titulo: " + titulo +
                "\n Autor: " + autor.getNome() +
                "\n Idiomas: " + idiomas +
                "\n Numero de descargas: " + numeroDeDownloads +
                "\n-----------------\n";
    }
}
