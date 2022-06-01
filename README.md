# Dokumentacja Iperf
1. ## Backend
     - ![błąd w wyświetlaniu obrazku](/Dokumentacja-Obrazki/StrukturaBackend.png "struktura backend") 
         - #### Package `data.archetypes` Klasy które są modelami uzytkownika jego ról oraz modelu logów konsoli oprócz tego getttery i settery
         - #### Package `data.repository` Interfejsy które zapewniają połączenie z bazą  
         - ```java
             //spring boot JpaRepository automatycznie wie, że ma wyszukać w bazie podaną zmienną 
              findByUsername(String username); 
              findByName(String name);
                 
         - #### Pacakge `service` zawiera dwie klasy które zawierają funkcjonalności związane z klasami z `data.archetypes`
             - `RequestService` `String requestExecute(...)` wykonuje i zapsiuje polecenie i dane na jego temat w bazie danych `String getRequestIp(...)` pobiera adres ip z nagłówka zapytania http z frontendu 
             - `UserSercice` `UserDetails loadUserByUsername(String username)` pobiera użytkownika z bazy i zwraca użytkownika w formacie UserDetails(wbudowane w springboot security) `User saveUser(User user)` zapisuje uzytkownika do bazy kodując jego haslo `Role saveRole(Role role)` zapisuje do bazy nową role `void addRoleToUser(String username, String roleName)` dodaje role do użytkownika `User getUser(String username)` pobiera użytkownika z bazy na podstawie podanej nazwy uzytkownika `List<User> getUsers()` pobiera liste wszystkich użytkwoników 
         -  #### Package `controller` zawiera kontrolery `REST API` wykorzystujące klasy pakietu `service`
             - `QueryResource` zawiera endpoint `/iperf/ping` wykonuje zapytanie konsolowe i zwraca je w odpowiedzi http. Limit zapytań to 20 łącznie co minute odnawiane 10. 
             - `UserResource` zawiera endpointy wykorzystujące metody z `UserService`.  `/api/users` zwraca wszystkichużytkowników z bazy. `/api/user/save` zapisuje uzytkownika przekazanego jako obiekt klasy user w body.`/api/role/save` zapisuje role przekazaną jako obiekt w body. `/api/role/addToUser` przypisuje role do uzytkownika w body przekazujemy obiekt podklasy `RoleToUserForm`.
            - ```java
                @Data
                class RoleToUserForm {
                    private String username;
                    private String roleName;
                }
            - `/api/token/refresh` zwraca `access_token` na podstawie `refresh_token` przechowywanego w ciasteczkutworzonego po stronie backendu po poprawnym logowaniu. Limit zapytań 8 na minute odnawiane 8 co minute dlacałej klasy.
        - #### Package `security` zawiera kalse `SecurityConfig` i package `filter`
            - `SecurityConfig`  `void configure(HttpSecurity http)`  konfiguracja zabezpieczeń.`CustomAuthenticationFilter customAuthenticationFilter = newCustomAuthenticationFilter(authenticationManagerBean());` ustawienie customowego uwierzytelnienia z`security.filter` nastepnie ustawiamy url pod którym będzie logowanie`customAuthenticationFilter.setFilterProcessesUrl("/login");` wyłączamy csrf ponieważ `JWT` zapobiega atakomcsrf. Ustawiamy kto ma dostęp do danych endpointów, ciąg dalszy ustawienia customowego uwierzytelnienia.`AuthenticationManager authenticationManagerBean()` wywołane na potrzebe ustawienia customowegouwierzytelnienia. `CorsConfigurationSource corsConfigurationSource()` konfiguracja cors pozwala nakomunikacje z frontendem.
        - #### Package `filter` zawiera customowe filtry uwierzytelnienia i upoważnienia 
            - `CustomAuthenticationFilter`  `Authentication attemptAuthentication(...)` próbuje uwierzytelnićużytkownika na podstawie `authenticationManager` (spring wie żeby pobrać z bazy samemu)
            - `void successfulAuthentication(...)` po udanym uwierzytelnieniu tworzy `access_token` i zwraca goużytkownikowi i zapisuje `refresh_token` do ciasteczka. `void unsuccessfulAuthentication(...)` zwracabłąd gdy użytkownik poda złe dane.
            - `CustomAuthorizationFilter` `void doFilterInternal(...)` najpierw sprawdza czy limit requestów niezostał przekroczony, później sprawdza czy to nie ścieżka `/login` lub `/api/token/refresh` jeśli nie tożąda nagłówka uwierzytelnienia jeśli go nie ma zwraca błąd jeśli jest to próbuje odkodować token `JWT` i na jego podstawie sprawdza czy użytkownik ma dostęp do resourca 
            - Package `utility` zawiera klase mającą funkcje do tworzenia tokenów `JWT`
2. ## Frontend
    - Struktura, projekt składa się z trzech komponentów.
        - ### `Header` najwyższy komponent zawiera nagłówek strony a także formularz logowania 
            - #### Stany:
                -   `JWT` przechowuje `access_token`
                -   `wasLoginSuccesfull` przechowuje wiadomość w wypadku błędu przy logowaniu 
                -   `showLoginForm` decyduje czy formularz do logowania powinien być widzoczny
                -   `loginCredentials` dane które użytkownik wpisał do formularza logowania
                -   `width` obecna szerokość okna
            - #### Animacja `loginFormAnim`, animacja pojawienia się formularza   
            - #### Funkcje:
                - `handleLoginForm` zmienia stan `loginCredentials` gdy użytkownik coś wpisuje do formularza logowania
                - `handleLoginFormSubmit` wysyła do backendu dane użytkownika, jeśli call się powiódło to zapisujemy zwrócony     token do stanu `JWT` jeśli call się nie powiódl to ustawiamy powiadomienie o błędzie
                - `useEffect` ładujący się podczas załadowania strony próbujący automatycznie zalogować użytkownika po odświerzeniu
        - ### `Main` podrzędny komponent zawiera formularz do tworzenia zapytań i okno z ich wynikiem. 
            - #### Stany (dziedziczone stany pominięte):
                - `query` przechowuje zapytanie wysyłane do serwera
                - `queryResponse` przechowuje wynik zapytania zwrócony przez serwer 
            - #### Animacje:
                - Do działania animacji potrzebne 2 stany ktore decydują czy są wyświetlane `loadAnimation`  i `textAnimation` nazwy animacji: `loaderAnim` i `textAnim`
            - #### Funkcje:
                - `handleQueryChange` zmienia stan `query` gdy użytkownik coś wpisuje do formualarza zapytania
                - `queryBuilder` dodaje do zapytania parametry na podstawie wartości kliknietego przycisku
                - `handleQueryFormSubmit` wysyła do backendu stan `query`, zapisujemy dane zwrócone z backendu do `queryResponse`
                - `capitalizeFirstLetter` prosta funkcja mająca zamienić pierwszą litere `Stringa` na dużą litere
3. ## Setup frontendu
    - ##### Potrzebne moduły powinny się zainstalować po wpisaniu komendy npm install w katalogu głównym
    - ##### Jeśli coś by się nie zaistalowało zostawiam liste użytych bibliotek 
        - ###### npm i react-css-loaders
        - ###### npm i dompurify
        - ###### npm i react-spinners
        - ###### npm i react-spring
        - ###### npm i @mui/icons-material
        - ###### npm i @emotion/styled
    - #### Oprócz tego w katalogu głównym w razie potrzeby utworzyć plik `.babelrc` z zawartością:
    - ```json
         {
            "presets": ["@babel/preset-react", "@babel/preset-env"],
            "plugins": ["@emotion"]
        }
    
               
