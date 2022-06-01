# Dokumentacja Iperf
1. ## Backend
    - Struktura: 
        - ![błąd w wyświetlaniu obrazku](/Dokumentacja-Obrazki/StrukturaBackend.png "struktura backend") 
            - Package `data.archetypes` Klasy które są modelami uzytkownika jego ról oraz modelu logów konsoli oprócz tego getttery i settery
            - Package `data.repository` Interfejsy które zapewniają połączenie z bazą  
            - ```java
                //spring boot JpaRepository automatycznie wie, że ma wyszukać w bazie podaną zmienną 
                 findByUsername(String username); 
                 findByName(String name);
                    
            - Pacakge `service` zawiera dwie klasy które zawierają funkcjonalności związane z klasami z `data.archetypes`
                - `RequestService` `String requestExecute(...)` wykonuje i zapsiuje polecenie i dane na jego temat w bazie danych `String getRequestIp(...)` pobiera adres ip z nagłówka zapytania http z frontendu 
                - `UserSercice` `UserDetails loadUserByUsername(String username)` pobiera użytkownika z bazy i zwraca użytkownika w formacie UserDetails(wbudowane w springboot security) `User saveUser(User user)` zapisuje uzytkownika do bazy kodując jego haslo `Role saveRole(Role role)` zapisuje do bazy nową role `void addRoleToUser(String username, String roleName)` dodaje role do użytkownika `User getUser(String username)` pobiera użytkownika z bazy na podstawie podanej nazwy uzytkownika `List<User> getUsers()` pobiera liste wszystkich użytkwoników 
            -  Package `controller` zawiera kontrolery `REST API` wykorzystujące klasy pakietu `service`
                - `QueryResource` zawiera endpoint `/iperf/ping` wykonuje zapytanie konsolowe i zwraca je w odpowiedzi http. Limit zapytań to 20 łącznie co minute odnawiane 10. 
                - `UserResource` zawiera endpointy wykorzystujące metody z `UserService`.  `/api/users` zwraca wszystkich użytkowników z bazy. `/api/user/save` zapisuje uzytkownika przekazanego jako obiekt klasy user w body. `/api/role/save` zapisuje role przekazaną jako obiekt w body. `/api/role/addToUser` przypisuje role do uzytkownika w body przekazujemy obiekt podklasy `RoleToUserForm`.
                - ```java
                    @Data
                    class RoleToUserForm {
                        private String username;
                        private String roleName;
                    }
               - `/api/token/refresh` zwraca `access_token` na podstawie `refresh_token` przechowywanego w ciasteczku tworzonego po stronie backendu po poprawnym logowaniu. Limit zapytań 8 na minute odnawiane 8 co minute dla całej klasy.
            - Package `security` zawiera kalse `SecurityConfig` i package `filter`
                - `SecurityConfig`  `void configure(HttpSecurity http)`  konfiguracja zabezpieczeń. `CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean());` ustawienie customowego uwierzytelnienia z `security.filter` nastepnie ustawiamy url pod którym będzie logowanie `customAuthenticationFilter.setFilterProcessesUrl("/login");` wyłączamy csrf ponieważ `JWT` zapobiega atakom csrf. Ustawiamy kto ma dostęp do danych endpointów, ciąg dalszy ustawienia customowego uwierzytelnienia. `AuthenticationManager authenticationManagerBean()` wywołane na potrzebe ustawienia customowego uwierzytelnienia. `CorsConfigurationSource corsConfigurationSource()` konfiguracja cors pozwala na komunikacje z frontendem.
                - Package `filter` zawiera customowe filtry uwierzytelnienia i upoważnienia 
                    - `CustomAuthenticationFilter`  `Authentication attemptAuthentication(...)` próbuje uwierzytelnić użytkownika na podstawie `authenticationManager` (spring wie żeby pobrać z bazy samemu)
                    - `void successfulAuthentication(...)` po udanym uwierzytelnieniu tworzy `access_token` i zwraca go użytkownikowi i zapisuje `refresh_token` do ciasteczka. `void unsuccessfulAuthentication(...)` zwraca błąd gdy użytkownik poda złe dane.
                    - `CustomAuthorizationFilter` `void doFilterInternal(...)` najpierw sprawdza czy limit requestów nie został przekroczony, później sprawdza czy to nie ścieżka `/login` lub `/api/token/refresh` jeśli nie to żąda nagłówka uwierzytelnienia jeśli go nie ma zwraca błąd jeśli jest to próbuje odkodować token `JWT` i na jego podstawie sprawdza czy użytkownik ma dostęp do resourca 
                - Package `utility` zawiera klase mającą funkcje do tworzenia tokenów `JWT`
2. ## Frontend
    -
                
