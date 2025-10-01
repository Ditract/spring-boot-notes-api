package com.sanez.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perfil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ElementCollection
    @CollectionTable(name = "perfil_notas_favoritas", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "nota_id")
    private List<Long> notasFavoritas = new ArrayList<>();
    //private String backupProvider = "GOOGLE_DRIVER";

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

}
