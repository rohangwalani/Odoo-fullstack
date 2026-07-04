import { Link } from 'react-router-dom';
import { Users, ArrowRight, ShieldCheck } from 'lucide-react';

export const WelcomePage = () => {
  return (
    <div className="welcome-container">
      {/* Reusing the beautiful immersive background */}
      <div className="auth-bg"></div>
      <div className="welcome-bg-image"></div>
      <div className="welcome-overlay"></div>
      <div className="auth-bg-grid"></div>

      <div className="welcome-content">
        <div className="welcome-brand">
          <div className="welcome-logo-box" style={{ background: '#3b6be3', borderRadius: '16px' }}>
            <span style={{ fontSize: '24px', fontWeight: 'bold', letterSpacing: '1px' }}>HRIS</span>
          </div>
        </div>

        <h2 className="welcome-headline">
          Welcome to HRIS
        </h2>
        <p className="welcome-subtitle" style={{ fontSize: '1.25rem', color: 'var(--text-muted)', marginBottom: '2rem' }}>
          Human Resource Information System
        </p>



        <div className="welcome-actions">
          <Link to="/login" className="welcome-btn welcome-btn-primary">
            Sign In to Portal
            <ArrowRight size={18} />
          </Link>
          
          <Link to="/register" className="welcome-btn welcome-btn-secondary">
            Register New Company
          </Link>
        </div>
      </div>
    </div>
  );
};
