import { FaTimes } from 'react-icons/fa';
import LoginForm from '../components/LoginForm';

const LoginPage = () => {
  const closeModal = () => {
    // gérer la fermeture de la modale
    console.log('Fermer la modale');
  };

  return (
    <div className="modal-overlay">
      <div className="modal-box">
        <button className="close-btn" onClick={closeModal}>
          <FaTimes />
        </button>
        <h1 className="modal-title">Connexion</h1>
        <LoginForm />
        <div className="form-footer">
          <p>Pas de compte ? <a href="#">Créer un compte</a></p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;