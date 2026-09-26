package com.example.drift;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "unmigrated_records")
public class UnmigratedRecord {

    @Id
    private Long id;

    private String label;
}
