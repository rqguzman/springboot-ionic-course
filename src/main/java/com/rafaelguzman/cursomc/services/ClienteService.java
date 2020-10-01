package com.rafaelguzman.cursomc.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rafaelguzman.cursomc.domain.Cidade;
import com.rafaelguzman.cursomc.domain.Cliente;
import com.rafaelguzman.cursomc.domain.Endereco;
import com.rafaelguzman.cursomc.domain.enums.Perfil;
import com.rafaelguzman.cursomc.domain.enums.TipoCliente;
import com.rafaelguzman.cursomc.dto.ClienteDTO;
import com.rafaelguzman.cursomc.dto.ClienteNewDTO;
import com.rafaelguzman.cursomc.repositories.ClienteRepository;
import com.rafaelguzman.cursomc.repositories.EnderecoRepository;
import com.rafaelguzman.cursomc.resources.ClienteResource;
import com.rafaelguzman.cursomc.security.UserSS;
import com.rafaelguzman.cursomc.services.exceptions.AuthorizationException;
import com.rafaelguzman.cursomc.services.exceptions.DataIntegrityException;
import com.rafaelguzman.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepository repo;
	
	@Autowired
	private EnderecoRepository enderecoRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	public Cliente find(Integer id) {
	
		UserSS user = UserService.authenticated();
		
		if (user == null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getId())) {
			throw new AuthorizationException("Acesso negado.");
		}
		
		Optional<Cliente> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
	}

	@Transactional
	public Cliente insert(Cliente obj) {
		obj.setId(null);// Garantindo que SEJA um novo objeto.
		obj = repo.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());
		return obj;
	}

	public Cliente update(Cliente obj) {
		Cliente newObj = find(obj.getId());
		updateData(newObj, obj);
		return repo.save(newObj);
	}

	public void delete(Integer id) {
		find(id);

		try {
			repo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir porque há pedidos relacionados.");
		}
	}

	public List<Cliente> findAll() {
		return repo.findAll();
	}

	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String direction, String orderBy) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}

	/**
	 * Mét. Aux. Convertendo um objeto ClienteDTO para Cliente
	 * 
	 * @param 1 objeto DTO do Tipo ClienteDTO
	 * @return 1 objeto do Tipo Cliente
	 * Aplicação:
	 * @see ClienteResource.java
	 */
	public Cliente fromDTO(ClienteDTO objDTO) {
		return new Cliente(objDTO.getId(), objDTO.getNome(), objDTO.getEmail(), null, null, null);
	}

	/**
	 * Mét. Aux. Convertendo um objeto ClienteNewDTO para Cliente
	 * Converte um cliente recebido no corpo da requisição.
	 * 
	 * @param 1 objeto DTO do Tipo ClienteNewDTO
	 * @return 1 objeto do Tipo Cliente 
	 * Aplicação:
	 * @see ClienteResource.java
	 */
	public Cliente fromDTO(ClienteNewDTO objDTO) {
		Cliente cli = new Cliente(null, objDTO.getNome(), objDTO.getEmail(), objDTO.getCpfOuCnpj(),
				TipoCliente.toEnum(objDTO.getTipo()), passwordEncoder.encode(objDTO.getSenha()));
		Cidade cid = new Cidade(objDTO.getCidadeId(), null, null);
		Endereco end = new Endereco(null, objDTO.getLogradouro(), objDTO.getNumero(), objDTO.getComplemento(),
				objDTO.getBairro(), objDTO.getCep(), cli, cid);
		cli.getEnderecos().add(end);
		cli.getTelefones().add(objDTO.getTelefone1());
		if (objDTO.getTelefone2() != null) {
			cli.getTelefones().add(objDTO.getTelefone2());			
		}
		if (objDTO.getTelefone3() != null) {
			cli.getTelefones().add(objDTO.getTelefone3());			
		}
		return cli;
	}

	/**
	 * Mét. Aux. Atualiza um objeto Cliente a partir de um Obj recebido como
	 * argumento Na atualização de um cliente, recebemos apenas o nome e o email
	 * desse cliente. o CPF/CNPJ e o Tipo do cliente permanecem inalterados. Para
	 * evitar que esses campos fiquem com o valor nulo, é preciso buscar essas
	 * informações na base de dados.
	 * 
	 * @param 1 objeto do Tipo Cliente
	 */
	private void updateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}
}
