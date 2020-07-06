package com.app.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.ProductoDto;
import com.app.entity.Categoria;
import com.app.entity.Producto;
import com.app.entity.Seller;
import com.app.service.CategoriaService;
import com.app.service.ProductoService;
import com.app.service.SellerService;
import com.app.util.Mensaje;

@RestController
@RequestMapping("/bfood/producto")
public class ApiProductController {

	@Autowired
	private ProductoService productoService;

	@Autowired
	private SellerService sellerService;

	@Autowired
	private CategoriaService categoriaService;

	// start method register product only by seller
	// ========================================================================================

	@PreAuthorize("hasRole('ROLE_VENDEDOR')")
	@PostMapping(path = "/registrar-producto", consumes = "application/json", produces = "application/json")
	public ResponseEntity<?> registrarProducto(@Valid @RequestBody ProductoDto dto) {
		try {
			
			System.out.println("ROLES");
			System.out.println(dto.toString());
			System.out.println("El código es:" + dto.getCategoria().getId());

			if (StringUtils.isBlank(dto.getNombre()))
				return new ResponseEntity<>(new Mensaje(false, "Ingrese nombre de producto"), HttpStatus.BAD_REQUEST);
			if (dto.getPrecio() == null || dto.getPrecio() < 0)
				return new ResponseEntity<>(new Mensaje(false, "el precio debe ser mayor de 0"),
						HttpStatus.BAD_REQUEST);
			if (valExistProduct(dto.getNombre()))
				return new ResponseEntity<>(new Mensaje(false, " el nombre de producto ya existe"),
						HttpStatus.BAD_REQUEST);

			Producto p = new Producto();
			p.setNombre(dto.getNombre());
			p.setDescripcion(dto.getDescripcion());
			p.setPrecio(dto.getPrecio());
			p.setPosition(dto.getPosition());
			Categoria categoria = categoriaService.get(dto.getCategoria().getId());
			p.setCategoria(categoria);
			p.setStock(dto.getStock());
			p.setImg(dto.getImg());

			p.setPublish(dto.isPublish());
			p.setStatus(dto.getStatus());
			Seller seller = sellerService.get(dto.getVendedor().getId());
			p.setVendedor(seller);
			productoService.save(p);
			return new ResponseEntity<>(new Mensaje(true, "Producto registrado"), HttpStatus.OK);
		} catch (NoSuchElementException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	boolean valExistProduct(String param) {
		List<Producto> list = productoService.read();
		for (Producto p : list) {
			if (p.getNombre().equals(param)) {
				return true;
			}
		}
		return false;
	}
	
	@PutMapping(path = "/actualizar-producto", consumes = "application/json", produces = "application/json")
	public ResponseEntity<?> updateProducto(@Valid @RequestBody Producto product) {
		try {
			
			System.out.println("El código es:" + product.getCategoria().getId());

			if (StringUtils.isBlank(product.getNombre()))
				return new ResponseEntity<>(new Mensaje(false, "Ingrese nombre de producto"), HttpStatus.BAD_REQUEST);
			if (product.getPrecio() < 0)
				return new ResponseEntity<>(new Mensaje(false, "el precio debe ser mayor de 0"),
						HttpStatus.BAD_REQUEST);
			if (valExistProduct(product.getNombre()))
				return new ResponseEntity<>(new Mensaje(false, " el nombre de producto ya existe"),
						HttpStatus.BAD_REQUEST);

			Producto p = productoService.get(product.getId());
			Categoria c = categoriaService.get(product.getCategoria().getId());
			p.setNombre(product.getNombre());
			p.setDescripcion(product.getDescripcion());
			p.setImg(product.getImg());
			p.setPosition(product.getPosition());
			p.setPrecio(product.getPrecio());
			p.setStatus(product.getStatus());
			p.setStock(product.getStock());
			
			p.setCategoria(c);
			
			productoService.save(p);
			
			return new ResponseEntity<>(new Mensaje(true, "Producto actualizado"), HttpStatus.OK);
		} catch (NoSuchElementException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@PutMapping(path = "/dlt", consumes = "application/json", produces = "application/json")
	public ResponseEntity<?> deleteProduct(@RequestBody String productId) {
		
		try {
			JSONObject request = new JSONObject(productId);
			int id = request.getInt("productId");

			System.out.println("El código es:" + productId);
			
			Producto product = productoService.get(id);
			
			product.setStatus("D");
			
			productoService.save(product);

			return new ResponseEntity<>(new Mensaje(true, "Producto eliminado"), HttpStatus.OK);
		} catch (NoSuchElementException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
	}

	// end method register product only by seller
	// ========================================================================================

	// start global methods for the page
	// ========================================================================================

	@GetMapping("/allProducts")
	public List<Producto> listartodo() {
		List<Producto> list = productoService.read();
		return list;
	}

	@GetMapping(path = "/ProductId/{id}", produces = "application/json")
	public ResponseEntity<?> encontrarById(@PathVariable("id") int id) {
		Producto producto = productoService.get(id);
		return new ResponseEntity<>(producto, HttpStatus.OK);
	}

	@GetMapping("/ProductName/{name}")
	public ResponseEntity<?> encontrarByName(@PathVariable("name") String name) {
		Producto producto = getProductoByName(name);
		if (producto == null)
			return new ResponseEntity<>(new Mensaje(false, "Producto no encontrado"), HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(producto, HttpStatus.OK);
	}

	Producto getProductoByName(String param) {

		Producto x = null;
		List<Producto> lst = productoService.read();
		for (Producto producto : lst) {
			if (producto.getNombre().equals(param)) {

				return producto;
			}
		}

		return x;
	}

	// end global methods for the page
	// ========================================================================================

	@GetMapping(path = "/listProductByCategory/{id}")
	public ResponseEntity<List<Producto>> listarByCategoria(@PathVariable("id") int id){
		
		List<Producto> lst = productoService.read().stream().filter(x -> x.getCategoria().getId() == id)
				.collect(Collectors.toList());
	
		if (lst.size()==0) {
			return new ResponseEntity<>(lst, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(lst, HttpStatus.OK);
	
	}
}
