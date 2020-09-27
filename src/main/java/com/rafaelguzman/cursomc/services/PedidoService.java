package com.rafaelguzman.cursomc.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rafaelguzman.cursomc.domain.ItemPedido;
import com.rafaelguzman.cursomc.domain.PagamentoComBoleto;
import com.rafaelguzman.cursomc.domain.Pedido;
import com.rafaelguzman.cursomc.domain.enums.EstadoPagamento;
import com.rafaelguzman.cursomc.repositories.ItemPedidoRepository;
import com.rafaelguzman.cursomc.repositories.PagamentoRepository;
import com.rafaelguzman.cursomc.repositories.PedidoRepository;
import com.rafaelguzman.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {

	@Autowired
	private PedidoRepository repo;
	
	@Autowired 
	private PagamentoRepository pagamentoRepository;
	
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	
	@Autowired
	private BoletoService boletoService;
	
	@Autowired
	private ProdutoService produtoService;

	@Autowired
	private ClienteService clienteService;
	
	public Pedido find(Integer id) {
		Optional<Pedido> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
                "Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
	}

	@Transactional
	public Pedido insert(Pedido obj) {

		obj.setId(null);// Garantindo que seja um novo pedido
		obj.setInstante(new Date());
		obj.setCliente(clienteService.find(obj.getCliente().getId()));
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if (obj.getPagamento() instanceof PagamentoComBoleto) {// "Preenche" uma data Venc.
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());
		}
		obj = repo.save(obj);// salva o pedido
		pagamentoRepository.save(obj.getPagamento());// salva o pagto
		for (ItemPedido itemPedido : obj.getItens()) {// percorre os itens de pedido
			itemPedido.setDesconto(0.0);// seta o valos do desconto
			itemPedido.setProduto(produtoService.find(itemPedido.getProduto().getId()));
			// localiza o preco do produto de um item e seta o valor:
			itemPedido.setPreco(itemPedido.getProduto().getPreco());
			itemPedido.setPedido(obj);// associa o item do pedido ao pedido
		}// prepara os itens para ser salvos no banco de dados
		
		itemPedidoRepository.saveAll(obj.getItens());
		System.out.println(obj);
		return obj;
	}
}
