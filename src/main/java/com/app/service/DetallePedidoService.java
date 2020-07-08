package com.app.service;

import org.springframework.stereotype.Service;

import com.app.common.Dao;
import com.app.entity.DetallePedido;

@Service
public interface DetallePedidoService extends Dao<DetallePedido, Integer> {

}