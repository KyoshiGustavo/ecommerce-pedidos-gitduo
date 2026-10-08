import { useCart } from '../contexts/CartContext';
import { Link } from 'react-router-dom';
import { Trash2, Plus, Minus, ArrowLeft, CreditCard } from 'lucide-react';

export function Cart() {
  const { cart, removeFromCart, updateQuantity, totalValue, clearCart } = useCart();

  if (cart.length === 0) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center">
        <h2 className="text-2xl font-bold text-gray-800 mb-4">O seu carrinho está vazio</h2>
        <p className="text-gray-600 mb-6">Explore o nosso catálogo e adicione alguns produtos!</p>
        <Link
          to="/"
          className="inline-flex items-center gap-2 bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition"
        >
          <ArrowLeft className="w-5 h-5" /> Voltar ao Catálogo
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">Carrinho de Compras</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-4">
          {cart.map((item) => (
            <div
              key={item.id}
              className="bg-white p-4 border rounded-xl shadow-sm flex items-center justify-between gap-4"
            >
              <img
                src={item.imagem}
                alt={item.nome}
                className="w-20 h-20 object-cover rounded-lg"
              />
              
              <div className="flex-1">
                <h3 className="font-bold text-gray-800">{item.nome}</h3>
                <p className="text-sm text-gray-500">
                  R$ {item.preco.toFixed(2)} cada
                </p>
              </div>

              <div className="flex items-center gap-2 border rounded-lg p-1">
                <button
                  onClick={() => updateQuantity(item.id, -1)}
                  className="p-1 hover:bg-gray-100 rounded text-gray-600"
                  aria-label="Diminuir quantidade"
                >
                  <Minus className="w-4 h-4" />
                </button>
                <span className="w-8 text-center font-semibold">{item.quantidade}</span>
                <button
                  onClick={() => updateQuantity(item.id, 1)}
                  className="p-1 hover:bg-gray-100 rounded text-gray-600"
                  aria-label="Aumentar quantidade"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>

              <div className="text-right">
                <p className="font-bold text-gray-900">
                  R$ {(item.preco * item.quantidade).toFixed(2)}
                </p>
                <button
                  onClick={() => removeFromCart(item.id)}
                  className="text-red-500 hover:text-red-700 p-1 mt-1"
                  title="Remover item"
                >
                  <Trash2 className="w-5 h-5" />
                </button>
              </div>
            </div>
          ))}

          <button
            onClick={clearCart}
            className="text-sm text-red-600 hover:underline font-medium"
          >
            Esvaziar carrinho
          </button>
        </div>

        {/* Resumo do Pedido */}
        <div className="bg-white p-6 border rounded-xl shadow-sm h-fit">
          <h2 className="text-xl font-bold text-gray-800 mb-4">Resumo do Pedido</h2>
          
          <div className="space-y-3 border-b pb-4 mb-4">
            <div className="flex justify-between text-gray-600">
              <span>Subtotal</span>
              <span>R$ {totalValue.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Envio</span>
              <span className="text-green-600 font-medium">Grátis</span>
            </div>
          </div>

          <div className="flex justify-between text-lg font-bold text-gray-900 mb-6">
            <span>Total</span>
            <span>R$ {totalValue.toFixed(2)}</span>
          </div>

          <button
            onClick={() => alert("Avançar para o checkout simulado!")}
            className="w-full bg-green-600 hover:bg-green-700 text-white py-3 rounded-lg font-bold flex items-center justify-center gap-2 transition"
          >
            <CreditCard className="w-5 h-5" /> Finalizar Compra
          </button>
        </div>
      </div>
    </div>
  );
}