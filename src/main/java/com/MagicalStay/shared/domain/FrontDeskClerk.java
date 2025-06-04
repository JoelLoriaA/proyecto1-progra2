package com.MagicalStay.shared.domain;

    import com.fasterxml.jackson.annotation.JsonCreator;
    import com.fasterxml.jackson.annotation.JsonProperty;

    public class FrontDeskClerk {
        private String name;
        private String lastNames;
        private String employeeId;
        private long dni;
        private String username;
        private String password;
        private int phoneNumber;
        private String user;

        // Constructor por defecto necesario para Jackson
        public FrontDeskClerk() {
        }

        // Constructor principal con anotaciones JsonCreator
        @JsonCreator
        public FrontDeskClerk(
                @JsonProperty("name") String name,
                @JsonProperty("lastNames") String lastNames,
                @JsonProperty("employeeId") String employeeId,
                @JsonProperty("phoneNumber") int phoneNumber,
                @JsonProperty("user") String user,
                @JsonProperty("password") String password) {
            this.name = name;
            this.lastNames = lastNames;
            this.employeeId = employeeId;
            this.phoneNumber = phoneNumber;
            this.user = user;
            this.password = password;
        }

        // Getters y setters originales
        @JsonProperty("name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JsonProperty("lastNames")
        public String getLastNames() {
            return lastNames;
        }

        public void setLastNames(String lastNames) {
            this.lastNames = lastNames;
        }

        @JsonProperty("employeeId")
        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        // Getters y setters para los campos faltantes
        @JsonProperty("phoneNumber")
        public int getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(int phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        @JsonProperty("user")
        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        @JsonProperty("password")
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }