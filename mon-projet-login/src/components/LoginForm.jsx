import { useState } from 'react';
import { FaEnvelope, FaLock, FaSignInAlt } from 'react-icons/fa';

const LoginForm = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    
    // Validation simple
    if (!email.includes('@')) {
      setError('Email invalide');
      return;
    }
    if (password.length < 6) {
      setError('Le mot de passe doit faire au moins 6 caractères');
      return;
    }

    console.log('Email:', email, 'Password:', password);
    // Ici tu pourras ajouter l'appel API pour la connexion
  };

  return (
    <form onSubmit={handleSubmit} className="login-form">
      {error && <div className="error-message">{error}</div>}
      
      <div className="form-group">
        <label htmlFor="email">
          <FaEnvelope className="input-icon" /> Email
        </label>
        <input
          type="email"
          id="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="exemple@email.com"
          required
        />
      </div>
      
      <div className="form-group">
        <label htmlFor="password">
          <FaLock className="input-icon" /> Mot de passe
        </label>
        <input
          type="password"
          id="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="••••••••"
          required
        />
      </div>
      
      <button type="submit" className="submit-btn">
        <FaSignInAlt className="btn-icon" /> Se connecter
      </button>
    </form>
  );
};

export default LoginForm;