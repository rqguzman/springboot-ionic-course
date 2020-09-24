package com.rafaelguzman.cursomc.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.rafaelguzman.cursomc.domain.Categoria;
import com.rafaelguzman.cursomc.domain.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer>{
	
	String consultaJPQL = "SELECT DISTINCT obj from Produto obj INNER JOIN  obj.categorias cat WHERE obj.nome LIKE %:nome% AND cat IN :categorias";
	
//	@Query(consultaJPQL)
//	public Page<Produto> search(@Param("nome") String nome, @Param("categorias") List<Categoria> categorias, Pageable pageRequest);
	
	// Fazendo a mesma query utilizando JPA Query method names
	// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods
	@Transactional(readOnly = true)
	public Page<Produto> findDistinctByNomeContainingAndCategoriasIn(String nome, List<Categoria> categorias, Pageable pageRequest);
	
}
