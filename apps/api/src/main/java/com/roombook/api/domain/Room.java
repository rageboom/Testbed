package com.roombook.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 회의실. 초기 데이터로 넣고 이 서비스에서는 읽기만 한다. */
@Entity
@Table(name = "ROOM")
public class Room {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "FLOOR", nullable = false)
    private int floor;

    @Column(name = "CAPACITY", nullable = false)
    private int capacity;

    protected Room() {
        // JPA 가 쓰는 기본 생성자
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getFloor() {
        return floor;
    }

    public int getCapacity() {
        return capacity;
    }
}
