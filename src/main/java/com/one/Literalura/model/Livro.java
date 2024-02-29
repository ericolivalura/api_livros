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
    private String idioma;
    private Double numeroDeDownloads;

    public Livro() {
    }

    public Livro(DadosLivro dadosLivro, Autor autor) {
        this.titulo = dadosLivro.titulo();
        this.autor = autor;
        this.idioma = dadosLivro.idiomas().get(0);
        this.numeroDeDownloads = dadosLivro.numeroDeDownloads();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getIdioma() {
        return idioma;
    }

    public Double getNumeroDeDownloads() {
        return numeroDeDownloads;
    }

    @Override
    public String toString() {
        return "----- LIVRO -----" +
                "\n Titulo: " + titulo +
                "\n Autor: " + autor.getNome() +
                "\n Idioma: " + idioma +
                "\n Número de downloads: " + numeroDeDownloads +
                "\n-----------------\n";
    }
}
