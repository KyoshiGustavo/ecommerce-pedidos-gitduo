import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { CartProvider } from './contexts/CartContext';
import { Navbar } from './components/Navbar';
import { Catalog } from './pages/Catalog';
import { Cart } from './pages/Cart';
import { ProductManagement } from './pages/ProductManagement';
import { CustomerManagement } from './pages/CustomerManagement';

export default function App() {
  return (
    <CartProvider>
      <Router>
        <div className="min-h-screen bg-slate-50 text-gray-900">
          <Navbar />
          <main>
            <Routes>
              <Route path="/" element={<Catalog />} />
              <Route path="/carrinho" element={<Cart />} />
              <Route path="/produtos" element={<ProductManagement />} />
              <Route path="/clientes" element={<CustomerManagement />} />
              <Route
                path="*"
                element={
                  <div className="text-center py-20 font-bold text-2xl">
                    404 - Página Não Encontrada
                  </div>
                }
              />
            </Routes>
          </main>
        </div>
      </Router>
    </CartProvider>
  );
}