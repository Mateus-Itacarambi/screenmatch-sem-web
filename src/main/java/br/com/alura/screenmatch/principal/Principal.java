package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodios;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporadas;
import br.com.alura.screenmatch.model.Episodio;
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

    public void exibeMenu() {
        System.out.println("Digite o nome da serie: ");
        var nomeSerrie = sc.nextLine();

        var json = consumoAPI.obterDados(ENDERECO + nomeSerrie.replace(" ", "+") + APIKEY);

        DadosSerie dadosSerie = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dadosSerie);

        List<DadosTemporadas> listaTemporada = new ArrayList<>();

        for (int i = 1; i <= dadosSerie.totalTemporadas(); i++) {
            json = consumoAPI.obterDados(ENDERECO + nomeSerrie.replace(" ", "+") + "&season=" + i + APIKEY);
            DadosTemporadas dadosTemporadas = conversor.obterDados(json, DadosTemporadas.class);
            listaTemporada.add(dadosTemporadas);
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

        List<DadosEpisodios> dadosEpisodios = listaTemporada.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

//        System.out.println("\nTop 10 Episodios:\n");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//                .sorted(Comparator.comparing(DadosEpisodios::avaliacao).reversed())
//                .limit(10)
//                .map(e -> e.titulo().toUpperCase())
//                .forEach(System.out::println);
//
        List<Episodio> episodios = listaTemporada.stream()
                .flatMap(t -> t.episodios().stream()
                    .map(d -> new Episodio(t.numero(), d)))
                .collect(Collectors.toList());

        episodios.forEach(System.out::println);

        System.out.println("Digite um trecho do título do episodio: ");
        var trechoTitulo = sc.nextLine();

        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();

        if (episodioBuscado.isPresent()) {
            System.out.println("Episódio encontrado");
            System.out.println("Temporada do episódio: " + episodioBuscado.get().getTemporada());
        } else {
            System.out.println("Episodio não encontrado");
        }
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

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Quantidade de episódios: " + est.getCount());
    }
}
