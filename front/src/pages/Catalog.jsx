import { useState } from 'react';
import { produtosMock } from '../services/mockData';
import { useCart } from '../contexts/CartContext';
import { Search, ShoppingBag } from 'lucide-react';

export function Catalog() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('Todas');
  const { addToCart } = useCart();

  const categories = ['Todas', ...new Set(produtosMock.map((p) => p.categoria))];

  const filteredProducts = produtosMock.filter((product) => {
    const matchesSearch = product.nome
      .toLowerCase()
      .includes(searchTerm.toLowerCase());
    const matchesCategory =
      selectedCategory === 'Todas' || product.categoria === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">Catálogo de Produtos</h1>

      <div className="flex flex-col md:flex-row gap-4 mb-8">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-3 text-gray-400 w-5 h-5" />
          <input
            type="text"
            placeholder="Buscar produto por nome..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
          />
        </div>

        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          className="px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 bg-white"
        >
          {categories.map((cat) => (
            <option key={cat} value={cat}>
              {cat}
            </option>
          ))}
        </select>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {filteredProducts.map((product) => (
          <div
            key={product.id}
            className="bg-white border rounded-xl shadow-sm hover:shadow-md transition overflow-hidden flex flex-col"
          >
            <img
              src={product.imagem}
              alt={product.nome}
              className="h-48 w-full object-cover"
            />
            <div className="p-4 flex-1 flex flex-col justify-between">
              <div>
                <span className="text-xs font-semibold text-blue-600 bg-blue-50 px-2 py-1 rounded">
                  {product.categoria}
                </span>
                <h2 className="text-lg font-bold text-gray-800 mt-2">
                  {product.nome}
                </h2>
                <p className="text-sm text-gray-500 mt-1">{product.descricao}</p>
              </div>

              <div className="mt-4 pt-4 border-t flex items-center justify-between">
                <span className="text-xl font-bold text-gray-900">
                  R$ {product.preco.toFixed(2)}
                </span>
                <button
                  onClick={() => addToCart(product)}
                  className="bg-blue-600 hover:bg-blue-700 text-white p-2 rounded-lg flex items-center gap-2 text-sm font-medium transition"
                  aria-label={`Adicionar ${product.nome} ao carrinho`}
                >
                  <ShoppingBag className="w-4 h-4" /> Adicionar
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}