package com.MagicalStay.shared.domain;

/**
 * Enum que define los roles de usuario en el sistema MagicalStay.
 * Los roles determinan los permisos y acceso a diferentes funcionalidades.
 */
public enum UserRole {
    /**
     * Rol de administrador con acceso completo al sistema.
     */
    ADMIN,

    /**
     * Rol de recepcionista con acceso limitado a funciones operativas.
     */
    FRONTDESK;

    /**
     * Verifica si el rol tiene permisos de administrador.
     * @return true si es rol ADMIN, false en caso contrario
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Verifica si el rol tiene permisos de recepcionista.
     * @return true si es rol FRONTDESK, false en caso contrario
     */
    public boolean isFrontDesk() {
        return this == FRONTDESK;
    }

    @Override
    public String toString() {
        return switch (this) {
            case ADMIN -> "Administrador";
            case FRONTDESK -> "Recepcionista";
        };
    }
}