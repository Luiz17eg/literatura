package com.alura.literatura.principal;

import com.alura.literatura.model.*;
import com.alura.literatura.repository.AutorRepository;
import com.alura.literatura.repository.LibroRepository;
import com.alura.literatura.service.ConsumoAPI;
import com.alura.literatura.service.ConvierteDatos;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibroRepository repositoryLibro;
    private AutorRepository repositoryAutor;
    private List<Autor> autores;
    private List<Libro> libros;

    public Principal(LibroRepository repositoryLibro, AutorRepository repositoryAutor) {
        this.repositoryLibro = repositoryLibro;
        this.repositoryAutor = repositoryAutor;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                        +---------------------------------------+
                        | 1 - Buscar libros por título          |
                        | 2 - Listar libros registrados         |
                        | 3 - Listar autores registrados        |
                        | 4 - Autores vivos en determinado año  |
                        | 5 - Listar libros por idioma          |
                        | 6 - Buscar Libro por autor            |
                        | 7 - Top 5 libros más descargados      |
                        | 8 - Libros con más y menos descargas  |
                        |                                       |
                        | 0 - Salir                             |
                        +---------------------------------------+
                        """;
            System.out.println("=== Menú de Challenge Literalura ===");
            System.out.println(menu);
            while (!teclado.hasNextInt()) {
                System.out.println("\033[1;31m¡Lo siento, la opción ingresada no es válida! Por favor, selecciona una opción disponible en el menú.\033[0m");
                teclado.nextLine();
            }
            opcion = teclado.nextInt();
            teclado.nextLine();
            switch (opcion) {
                case 1:
                    buscarLibro();
                    break;
                case 2:
                    listarLibros();
                    break;
                case 3:
                    listarAutores();
                    break;
                case 4:
                    autoresVivosPorAnio();
                    break;
                case 5:
                    buscarLibroPorIdioma();
                    break;
                case 6:
                    buscarLibroPorAutor();
                    break;
                case 7:
                    top5LibrosMasDescargados();
                    break;
                case 8:
                    ResumenLibrosRegistrados();
                    break;
                case 0:
                    System.out.println("\033[1;32mCerrando la aplicación...\033[0m");
                    break;
                default:
                    mostrarMensajeError("¡Opción inválida! Por favor, selecciona una opción disponible en el menú.");
            }
        }
    }

    private static void mostrarMensajeError(String mensaje) {
        System.out.println("\033[1;31m" + mensaje + "\033[0m");
    }
    private DatosBusqueda getBusqueda() {
        System.out.println("Escribe el nombre del libro: ");
        var nombreLibro = teclado.nextLine();
        try {
            var json = consumoAPI.obtenerDatos(URL_BASE + nombreLibro.replace(" ", "%20"));
            if (json != null && !json.isEmpty()) {
                return conversor.obtenerDatos(json, DatosBusqueda.class);
            } else {
                System.out.println("No se encontraron resultados para la búsqueda.");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error al buscar el libro: " + e.getMessage());
            return null;
        }
    }
    private void buscarLibro() {
        DatosBusqueda datosBusqueda = getBusqueda();
        if (datosBusqueda != null && !datosBusqueda.resultado().isEmpty()) {
            DatosLibro primerLibro = datosBusqueda.resultado().get(0);

            Libro libro = new Libro(primerLibro);
            System.out.println("\n+-----------------------------+");
            System.out.println("|           Libro             |");
            System.out.println(libro);

            Optional<Libro> libroExiste = repositoryLibro.findByTitulo(libro.getTitulo());
            if (libroExiste.isPresent()){
                System.out.println("\n\033[1;32mEl libro ya esta registrado...\033[0m\n");
            }else {

                if (!primerLibro.autor().isEmpty()) {
                    DatosAutor autor = primerLibro.autor().get(0);
                    Autor autor1 = new Autor(autor);
                    Optional<Autor> autorOptional = repositoryAutor.findByNombre(autor1.getNombre());

                    if (autorOptional.isPresent()) {
                        Autor autorExiste = autorOptional.get();
                        libro.setAutor(autorExiste);
                        repositoryLibro.save(libro);
                    } else {
                        Autor autorNuevo = repositoryAutor.save(autor1);
                        libro.setAutor(autorNuevo);
                        repositoryLibro.save(libro);
                    }

                    Integer numeroDescargas = libro.getNumero_descargas() != null ? libro.getNumero_descargas() : 0;
                    System.out.println("\n+-----------------------------+");
                    System.out.println("|           Libro             |");
                    System.out.println("+-----------------------------+");
                    System.out.printf("| Titulo: %s%n| Autor: %s%n| Idioma: %s%n| Numero de Descargas: %s%n",
                            libro.getTitulo(), autor1.getNombre(), libro.getLenguaje(), libro.getNumero_descargas());
                    System.out.println("+-----------------------------+\n");
                } else {
                    System.out.println("Sin autor");
                }
            }
        } else {
            System.out.println("\n\033[1;31mLibro no encontrado.\033[0m\n");
        }
    }
    private void listarLibros() {
        libros = repositoryLibro.findAll();
        libros.stream()
                .forEach(System.out::println);
    }
    private void listarAutores() {
        autores = repositoryAutor.findAll();
        autores.stream()
               .forEach(System.out::println);
    }
    private void autoresVivosPorAnio() {
        System.out.println("Ingresa el año de los autores vivos que deseas buscar: ");
        var anio = teclado.nextInt();
        teclado.nextLine();
        autores = repositoryAutor.listaAutoresVivosPorAnio(anio);
        autores.stream()
               .forEach(System.out::println);
    }
    private List<Libro> datosBusquedaLenguaje(String idioma) {
        Idioma dato = Idioma.fromString(idioma);
        System.out.printf("Buscando libros en el idioma: %s\n", dato);

        List<Libro> libroPorIdioma = repositoryLibro.findByLenguaje(dato);

        if (libroPorIdioma.isEmpty()) {
            System.out.printf("\n\033[1;31mNo se encontraron libros en el idioma %s.\033[0m\n", dato);
        }

        return libroPorIdioma;
    }
    private void buscarLibroPorIdioma() {
        System.out.println("Selecciona el idioma que deseas buscar:");

        int opcion = -1;
        while (opcion != 0) {
            String opciones = """
                        +---------------------------------------+
                        | 1 - Inglés [en]                       |
                        | 2 - Español [es]                      |
                        | 3 - Francés [fr]                      |
                        | 4 - Portugués [pt]                    |
                        |                                       |
                        | 0 - Volver a las opciones anteriores  |
                        +---------------------------------------+
                        """;
            System.out.println(opciones);

            while (!teclado.hasNextInt()) {
                System.out.println("\033[1;31m¡Lo siento, la opción ingresada no es válida! Por favor, selecciona una opción disponible en el menú.\033[0m");
                teclado.nextLine();
            }

            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    List<Libro> librosEnIngles = datosBusquedaLenguaje("[en]");
                    if (!librosEnIngles.isEmpty()) {
                        System.out.println("\nLibros en Inglés:");
                        librosEnIngles.forEach(System.out::println);
                    }
                    break;
                case 2:
                    List<Libro> librosEnEspanol = datosBusquedaLenguaje("[es]");
                    if (!librosEnEspanol.isEmpty()) {
                        System.out.println("\nLibros en Español:");
                        librosEnEspanol.forEach(System.out::println);
                    }
                    break;
                case 3:
                    List<Libro> librosEnFrances = datosBusquedaLenguaje("[fr]");
                    if (!librosEnFrances.isEmpty()) {
                        System.out.println("\nLibros en Francés:");
                        librosEnFrances.forEach(System.out::println);
                    }
                    break;
                case 4:
                    List<Libro> librosEnPortugues = datosBusquedaLenguaje("[pt]");
                    if (!librosEnPortugues.isEmpty()) {
                        System.out.println("\nLibros en Portugués:");
                        librosEnPortugues.forEach(System.out::println);
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("\033[1;31mOpción inválida, por favor selecciona una opción del menú.\033[0m");
            }
        }
    }
    private void buscarLibroPorAutor() {
        System.out.println("Ingresa el nombre del autor que deseas buscar: ");
        String nombreAutor = teclado.nextLine();
        List<Libro> librosPorAutor = repositoryLibro.findByAutor_Nombre(nombreAutor);

        if (librosPorAutor.isEmpty()) {
            System.out.printf("\n\033[1;31mNo se encontraron libros del autor %s.\033[0m\n", nombreAutor);
        } else {
            System.out.println("\nLibros del autor " + nombreAutor + ":");
            librosPorAutor.forEach(System.out::println);
        }
    }
    private void top5LibrosMasDescargados() {
        List<Libro> top5Libros = repositoryLibro.top5LibrosMasDescargados();

        if (top5Libros.isEmpty()) {
            System.out.println("\n\033[1;31mNo se encontraron libros con más descargas.\033[0m\n");
        } else {
            System.out.println("\nTop 5 Libros con más descargas:");
            top5Libros.forEach(System.out::println);
        }
    }
    private void ResumenLibrosRegistrados() {
        libros = repositoryLibro.findAll();
        IntSummaryStatistics est = libros.stream()
                .filter(l -> l.getNumero_descargas() > 0)
                .collect(Collectors.summarizingInt(Libro::getNumero_descargas));

        Libro libroMasDescargado = libros.stream()
                .filter(l -> l.getNumero_descargas() == est.getMax())
                .findFirst()
                .orElse(null);

        Libro libroMenosDescargado = libros.stream()
                .filter(l -> l.getNumero_descargas() == est.getMin())
                .findFirst()
                .orElse(null);
        System.out.println("+----------------------------------------------------+");
        System.out.printf("%nLibro más descargado: %s%nNúmero de descargas: " +
                        "%d%n%nLibro menos descargado: %s%nNúmero de descargas: " +
                        "%d%n%n",libroMasDescargado.getTitulo(),est.getMax(),
                libroMenosDescargado.getTitulo(),est.getMin());
        System.out.println("+----------------------------------------------------+");
    }

}
