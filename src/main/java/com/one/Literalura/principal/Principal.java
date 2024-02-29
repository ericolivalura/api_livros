package com.one.Literalura.principal;

import com.one.Literalura.model.*;
import com.one.Literalura.repository.AutorRepository;
import com.one.Literalura.repository.LivroRepository;
import com.one.Literalura.service.ConsumoAPI;
import com.one.Literalura.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Principal {

    @Autowired
    private final LivroRepository livroRepository;
    @Autowired
    private final AutorRepository autorRepository;

    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();
    private Scanner teclado = new Scanner(System.in);
    private String json;

    private String menu = """
            ------------
            Escolha o número de sua opção:
            1- buscar livro pelo título
            2- listar livros registrados
            3- listar autores registrados
            4- listar autores vivos em um determinado ano
            5- listar livros em um determinado idioma
            0 - sair""";

    public Principal(LivroRepository livroRepository, AutorRepository autorRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
    }

    public void mostrarMenu() {

        var opcaoEscolhida = -1;
        while (opcaoEscolhida != 0) {
            json = consumoAPI.obterDados(URL_BASE);
            System.out.println(menu);
            opcaoEscolhida = teclado.nextInt();
            teclado.nextLine();
            switch (opcaoEscolhida) {
                case 1 -> buscarLivroPeloTitulo();
                case 2 -> listarLivrosRegistrados();
                case 3 -> listarAutoresRegistrados();
                case 4 -> listarAutoresVivosEnAnoEspecifico();
                case 5 -> listarLivrosPorIdioma();
                case 0 -> System.out.println("Até logo...");
                default -> System.out.println("Opção inválida");
            }
        }
    }

    private void buscarLivroPeloTitulo() {
        DadosLivro dadosLivro = getDatosLivro();
        if (dadosLivro != null) {
            Livro livro;
            DadosAutor dadosAutor = dadosLivro.autor().get(0);
            Autor autorExistente = autorRepository.findByNome(dadosAutor.nome());

            if (autorExistente != null) {
                livro = new Livro(dadosLivro, autorExistente);
            } else {
                Autor autor = new Autor(dadosLivro.autor().get(0));
                livro = new Livro(dadosLivro, autor);
                autorRepository.save(autor);
            }
            try {
                livroRepository.save(livro);
                System.out.println(livro);
            } catch (Exception e) {
                System.out.println("Não é possível registrar o mesmo livro mais de uma vez");
            }

        } else {
            System.out.println("Livro não encontrado");
        }
    }

    private DadosLivro getDatosLivro() {
        System.out.println("Insira o nome do livro que você deseja procurar");
        var tituloLivro = teclado.nextLine();

        if (!tituloLivro.isBlank()) {
            json = consumoAPI.obterDados(URL_BASE + "?search=" + tituloLivro.replace(" ", "+"));
            var dadosPesquisa = conversor.obterDados(json, Dados.class);
            Optional<DadosLivro> livroBuscado = dadosPesquisa.resultados().stream()
                    .filter(l -> l.titulo().toUpperCase().contains(tituloLivro.toUpperCase()))
                    .findFirst();
            if (livroBuscado.isPresent()) {
                return livroBuscado.get();
            }
        } else {
            System.out.println("Campo de texto vazio, por favor, tente novamente e insira um texto válido.");
        }
        return null;
    }

    private void listarLivrosRegistrados() {
        livroRepository.findAll(Sort.by(Sort.Direction.ASC, "titulo")).forEach(System.out::println);
    }

    private void listarAutoresRegistrados() {
        autorRepository.findAll().forEach(System.out::println);
    }

    /* Consultas com datas (Autores) */

    private void listarAutoresVivosEnAnoEspecifico() {
        System.out.println("Insira o ano que deseja pesquisar");
        var anoVivo = teclado.nextLine();

        if (!anoVivo.isBlank()) {
            List<Autor> autoresBuscados = autorRepository.findByAnoDeNascimentoLessThanEqualAndAnoDeFalecimentoGreaterThan(
                    Integer.valueOf(anoVivo),
                    Integer.valueOf(anoVivo));

            if (!autoresBuscados.isEmpty()) {
                autoresBuscados.forEach(autor -> System.out.println(autor.toString()));
            } else {
                System.out.println("Não foram encontrados autores vivos nesse ano.");
            }
        } else {
            System.out.println("Campo vazio, por favor, tente novamente e insira um número inteiro válido.");
        }
    }


    private void listarLivrosPorIdioma() {
        var perguntaIdioma = """
                Insira o idioma para realizar a busca:
                es- espanhol
                en- inglês
                fr- francês
                pt- português
                """;
        System.out.println(perguntaIdioma);
        var idioma = teclado.nextLine();

        if (!idioma.isBlank()) {
            List<Livro> livrosPorIdioma = livroRepository.findByIdiomaEqualsIgnoreCase(idioma);
            if (livrosPorIdioma.isEmpty()) {
                System.out.println("Não existem livros nesse idioma no banco de dados.");
            } else {
                livrosPorIdioma.forEach(System.out::println);
            }
        } else {
            System.out.println("Campo de texto vazio, por favor, tente novamente e insira uma opção válida.");
        }
    }
}