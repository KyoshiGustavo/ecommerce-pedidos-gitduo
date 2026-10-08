import { useState } from 'react';
import { produtosMock } from '../services/mockData';
import { Plus, Trash2, Edit } from 'lucide-react';

export function ProductManagement() {
  const [produtos, setProdutos] = useState(produtosMock);
  const [novoProduto, setNovoProduto] = useState({ nome: '', preco: '', categoria: '', estoque: '' });

  const handleAddProduct = (e) => {
    e.preventDefault();
    if (!novoProduto.nome || !novoProduto.preco) return;
    
    const product = {
      id: Date.now(),
      nome: novoProduto.nome,
      preco: parseFloat(novoProduto.preco),
      categoria: novoProduto.categoria || 'Geral',
      estoque: parseInt(novoProduto.estoque) || 0,
      imagem: 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500&q=80',
      descricao: 'Novo produto cadastrado'
    };

    setProdutos([...produtos, product]);
    setNovoProduto({ nome: '', preco: '', categoria: '', estoque: '' });
  };

  const handleDeleteProduct = (id) => {
    if (confirm("Tem certeza que deseja excluir este produto?")) {
      setProdutos(produtos.filter(p => p.id !== id));
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">Gestão de Produtos</h1>

      {/* Formulário de Cadastro */}
      <form onSubmit={handleAddProduct} className="bg-white p-6 rounded-xl border shadow-sm mb-8 grid grid-cols-1 md:grid-cols-4 gap-4">
        <input
          type="text"
          placeholder="Nome do Produto"
          value={novoProduto.nome}
          onChange={(e) => setNovoProduto({ ...novoProduto, nome: e.target.value })}
          className="p-2 border rounded-lg"
          required
        />
        <input
          type="number"
          placeholder="Preço (R$)"
          value={novoProduto.preco}
          onChange={(e) => setNovoProduto({ ...novoProduto, preco: e.target.value })}
          className="p-2 border rounded-lg"
          required
        />
        <input
          type="text"
          placeholder="Categoria"
          value={novoProduto.categoria}
          onChange={(e) => setNovoProduto({ ...novoProduto, categoria: e.target.value })}
          className="p-2 border rounded-lg"
        />
        <button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white font-bold p-2 rounded-lg flex items-center justify-center gap-2">
          <Plus className="w-5 h-5" /> Cadastrar
        </button>
      </form>

      {/* Tabela de Produtos */}
      <div className="bg-white border rounded-xl shadow-sm overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-100 border-b text-gray-700">
              <th className="p-4">Nome</th>
              <th className="p-4">Categoria</th>
              <th className="p-4">Preço</th>
              <th className="p-4">Estoque</th>
              <th className="p-4 text-center">Ações</th>
            </tr>
          </thead>
          <tbody>
            {produtos.map((p) => (
              <tr key={p.id} className="border-b hover:bg-slate-50">
                <td className="p-4 font-semibold">{p.nome}</td>
                <td className="p-4">{p.categoria}</td>
                <td className="p-4">R$ {p.preco.toFixed(2)}</td>
                <td className="p-4">{p.estoque} un.</td>
                <td className="p-4 flex justify-center gap-2">
                  <button onClick={() => handleDeleteProduct(p.id)} className="text-red-600 hover:text-red-800 p-1">
                    <Trash2 className="w-5 h-5" />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}