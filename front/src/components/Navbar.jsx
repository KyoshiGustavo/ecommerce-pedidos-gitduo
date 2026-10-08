import { Link } from 'react-router-dom';
import { ShoppingCart, Package, Users, Store } from 'lucide-react';
import { useCart } from '../contexts/CartContext';

export function Navbar() {
  const { totalItems } = useCart();

  return (
    <header className="bg-slate-900 text-white shadow-md">
      <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 text-xl font-bold text-blue-400">
          <Store className="w-6 h-6" />
          <span>TechStore</span>
        </Link>

        <div className="flex items-center gap-6">
          <Link to="/" className="hover:text-blue-400 flex items-center gap-1 text-sm font-medium">
            Catálogo
          </Link>
          <Link to="/produtos" className="hover:text-blue-400 flex items-center gap-1 text-sm font-medium">
            <Package className="w-4 h-4" /> Gestão Produtos
          </Link>
          <Link to="/clientes" className="hover:text-blue-400 flex items-center gap-1 text-sm font-medium">
            <Users className="w-4 h-4" /> Clientes
          </Link>
          <Link
            to="/carrinho"
            className="relative p-2 text-gray-300 hover:text-white"
            aria-label={`Carrinho com ${totalItems} itens`}
          >
            <ShoppingCart className="w-6 h-6" />
            {totalItems > 0 && (
              <span className="absolute -top-1 -right-1 bg-blue-600 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                {totalItems}
              </span>
            )}
          </Link>
        </div>
      </nav>
    </header>
  );
}git add .
git commit -m "feat: adiciona componente Navbar e ajusta estrutura do frontend"
git push origin feature/testes-unitarios