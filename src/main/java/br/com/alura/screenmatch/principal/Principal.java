package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieReposity;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "http://www.omdbapi.com/?t=";
    private final String APIKEY = "&apikey=2c1b2ae1";
    private List<DadosSerie> dadosSerie = new ArrayList<>();
    private SerieReposity repositorio;

    public Principal(SerieReposity repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while(opcao != 0) {
            var menu = """
            1 - Buscar séries
            2 - Buscar episódios
            3 - Listar séries buscadas

            0 - Sair
            """;

            System.out.println(menu);
            opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }

//        listaTemporada.forEach(System.out::println);

//        for (int i = 0; i < dadosSerie.totalTemporadas(); i++) {
//            List<DadosEpisodios> listaEpisodios = listaTemporada.get(i).episodios();
//
//            for (int j = 0; j < listaEpisodios.size(); j++) {
//                System.out.println(listaEpisodios.get(j).titulo());
//            }
//        }

//        listaTemporada.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

//        List<DadosEpisodios> dadosEpisodios = listaTemporada.stream()
//                .flatMap(t -> t.episodios().stream())
//                .collect(Collectors.toList());

//        System.out.println("\nTop 10 Episodios:\n");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//                .sorted(Comparator.comparing(DadosEpisodios::avaliacao).reversed())
//                .limit(10)
//                .map(e -> e.titulo().toUpperCase())
//                .forEach(System.out::println);
//
//        List<Episodio> episodios = listaTemporada.stream()
//                .flatMap(t -> t.episodios().stream()
//                    .map(d -> new Episodio(t.numero(), d)))
//                .collect(Collectors.toList());

//        episodios.forEach(System.out::println);
//
//        System.out.println("Digite um trecho do título do episodio: ");
//        var trechoTitulo = sc.nextLine();
//
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
//                .findFirst();
//
//        if (episodioBuscado.isPresent()) {
//            System.out.println("Episódio encontrado");
//            System.out.println("Temporada do episódio: " + episodioBuscado.get().getTemporada());
//        } else {
//            System.out.println("Episodio não encontrado");
//        }
//
//        System.out.println("A Partir de que ano você deseja ver os episódios? ");
//        var ano = sc.nextInt();
//        sc.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//                .forEach(e -> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                                "\nEpisódio: " + e.getTitulo() +
//                                "\nData Lançamento: " + e.getDataLancamento().format(formatter) + "\n"
//                ));
//
//        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
//                .filter(e -> e.getAvaliacao() > 0.0)
//                .collect(Collectors.groupingBy(Episodio::getTemporada,
//                        Collectors.averagingDouble(Episodio::getAvaliacao)));
//
//        System.out.println(avaliacoesPorTemporada);
//
//        DoubleSummaryStatistics est = episodios.stream()
//                .filter(e -> e.getAvaliacao() > 0.0)
//                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
//
//        System.out.println("Média: " + est.getAverage());
//        System.out.println("Melhor episódio: " + est.getMax());
//        System.out.println("Pior episódio: " + est.getMin());
//        System.out.println("Quantidade de episódios: " + est.getCount());
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        //dadosSerie.add(dados);
        Serie serie = new Serie(dados);
        repositorio.save(serie);
        System.out.println(dados + "\n");
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da serie: ");
        var nomeSerie = sc.nextLine();

        var json = consumoAPI.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + APIKEY);

        DadosSerie dadosSerie = conversor.obterDados(json, DadosSerie.class);
        return dadosSerie;
    }

    private void buscarEpisodioPorSerie() {
        DadosSerie dadosSerie = getDadosSerie();
        List<DadosTemporadas> listaTemporada = new ArrayList<>();

        for (int i = 1; i <= dadosSerie.totalTemporadas(); i++) {
            var json = consumoAPI.obterDados(ENDERECO + dadosSerie.titulo().replace(" ", "+") + "&season=" + i + APIKEY);
            DadosTemporadas dadosTemporadas = conversor.obterDados(json, DadosTemporadas.class);
            listaTemporada.add(dadosTemporadas);
        }

        listaTemporada.stream().forEach(System.out::println);
        System.out.println();
    }

    private void listarSeriesBuscadas() {
        List<Serie> series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

        System.out.println();
    }
}
