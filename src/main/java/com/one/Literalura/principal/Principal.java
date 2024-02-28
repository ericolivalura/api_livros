package com.one.Literalura.principal;

import com.one.Literalura.model.*;
import com.one.Literalura.repository.AutorRepository;
import com.one.Literalura.repository.LivroRepository;
import com.one.Literalura.service.ConsumoAPI;
import com.one.Literalura.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

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
            5- exibir estatísticas de downloads dos livros registrados
            0 - sair
            """;

    public Principal(LivroRepository livroRepository, AutorRepository autorRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {

        var opcionElegida = -1;
        while (opcionElegida != 0) {
            json = consumoAPI.obterDados(URL_BASE);
            System.out.println(menu);
            opcionElegida = teclado.nextInt();
            teclado.nextLine();
            switch (opcionElegida) {
                case 1 -> buscarLivroPeloTitulo();
                case 2 -> listarLibrosRegistrados();
                case 3 -> listarAutoresRegistrados();
                case 4 -> listarAutoresVivosEnAnoEspecifico();
                case 5 -> exhibirEstadisticasDeDescargasDeLosLibrosRegistrados();
                case 0 -> System.out.println("Até logo...");
                default -> System.out.println("Opção inválida");
            }
        }
    }

    private void buscarLivroPeloTitulo() {
        DadosLivro dadosLivro = getDatosLibro();
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

    private DadosLivro getDatosLibro() {
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

    private void listarLibrosRegistrados() {
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


    private void exhibirEstadisticasDeDescargasDeLosLibrosRegistrados() {
        // Quantidade de livros por idioma
        Map<List<String>, Long> livrosPorIdioma = livroRepository.findAll().stream()
                .collect(Collectors.groupingBy(Livro::getIdiomas, Collectors.counting()
                ));

        System.out.println("Quantidade de livros por idioma");
        livrosPorIdioma.forEach((idioma, totalLivros) ->
                System.out.println(idioma + ": " + totalLivros)); //

        // Cantidad de descargas por idiomas
        Map<List<String>, Double> descargasPorIdioma = livroRepository.findAll().stream()
                .collect(Collectors.groupingBy(Livro::getIdiomas, Collectors.summingDouble(Livro::getNumeroDeDownloads)
                ));

        System.out.println("Quantidade de livros baixados por idioma");
        descargasPorIdioma.forEach((idioma, totalDeDownloads) ->
                System.out.println(idioma + ": " + totalDeDownloads));
    }
}