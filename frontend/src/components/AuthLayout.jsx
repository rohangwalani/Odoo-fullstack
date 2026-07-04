import { motion } from 'framer-motion';

export const AuthLayout = ({ children, title, subtitle }) => {
  return (
    <>
      <div className="auth-bg" />
      <div className="auth-bg-grid" />
      <main className="page-container">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, ease: [0.4, 0, 0.2, 1] }}
          className="auth-card"
        >
          <div className="brand-logo justify-center" style={{ marginBottom: '1rem' }}>
            <div style={{ background: '#3b6be3', borderRadius: '12px', width: '56px', height: '56px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}>
              <span style={{ fontSize: '18px', fontWeight: 'bold', letterSpacing: '0.5px' }}>HRIS</span>
            </div>
          </div>

          <div className="text-center mb-6">
            <h1 className="auth-title">{title}</h1>
            {subtitle && <p className="auth-subtitle">{subtitle}</p>}
          </div>

          {children}
        </motion.div>
      </main>
    </>
  );
};
