import { useState } from 'react';
import { clientesMock } from '../services/mockData';
import { UserPlus, Trash2 } from 'lucide-react';

export function CustomerManagement() {
  const [clientes, setClientes] = useState(clientesMock);
  const [novoCliente, setNovoCliente] = useState({ nome: '', email: '', documento: '', telefone: '' });

  const handleAddCustomer = (e) => {
    e.preventDefault();
    if (!novoCliente.nome || !novoCliente.email) return;

    setClientes([...clientes, { id: Date.now(), ...novoCliente }]);
    setNovoCliente({ nome: '', email: '', documento: '', telefone: '' });
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">Gestão de Clientes</h1>

      <form onSubmit={handleAddCustomer} className="bg-white p-6 rounded-xl border shadow-sm mb-8 grid grid-cols-1 md:grid-cols-4 gap-4">
        <input
          type="text"
          placeholder="Nome Completo"
          value={novoCliente.nome}
          onChange={(e) => setNovoCliente({ ...novoCliente, nome: e.target.value })}
          className="p-2 border rounded-lg"
          required
        />
        <input
          type="email"
          placeholder="E-mail"
          value={novoCliente.email}
          onChange={(e) => setNovoCliente({ ...novoCliente, email: e.target.value })}
          className="p-2 border rounded-lg"
          required
        />
        <input
          type="text"
          placeholder="CPF"
          value={novoCliente.documento}
          onChange={(e) => setNovoCliente({ ...novoCliente, documento: e.target.value })}
          className="p-2 border rounded-lg"
        />
        <button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white font-bold p-2 rounded-lg flex items-center justify-center gap-2">
          <UserPlus className="w-5 h-5" /> Cadastrar
        </button>
      </form>

      <div className="bg-white border rounded-xl shadow-sm overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-100 border-b text-gray-700">
              <th className="p-4">Nome</th>
              <th className="p-4">E-mail</th>
              <th className="p-4">CPF</th>
            </tr>
          </thead>
          <tbody>
            {clientes.map((c) => (
              <tr key={c.id} className="border-b hover:bg-slate-50">
                <td className="p-4 font-semibold">{c.nome}</td>
                <td className="p-4">{c.email}</td>
                <td className="p-4">{c.documento || 'N/A'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}