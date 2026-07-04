import { useEffect, useState } from 'react';

const getStrength = (password) => {
  if (!password) return { score: 0, label: '', className: '' };
  
  let score = 0;
  if (password.length >= 8) score += 1;
  if (/[a-z]/.test(password)) score += 1;
  if (/[A-Z]/.test(password)) score += 1;
  if (/[0-9]/.test(password)) score += 1;
  if (/[^a-zA-Z0-9]/.test(password)) score += 1;

  if (score <= 2) return { score, label: 'Weak', className: 'weak' };
  if (score === 3) return { score, label: 'Fair', className: 'fair' };
  if (score === 4) return { score, label: 'Good', className: 'good' };
  return { score, label: 'Strong', className: 'strong' };
};

export const PasswordStrength = ({ password }) => {
  const [strength, setStrength] = useState({ score: 0, label: '', className: '' });

  useEffect(() => {
    setStrength(getStrength(password));
  }, [password]);

  if (!password) return null;

  return (
    <div className="strength-container mt-1">
      <div className="strength-bars">
        <div className={`strength-bar ${strength.score >= 1 ? strength.className : ''}`} />
        <div className={`strength-bar ${strength.score >= 3 ? strength.className : ''}`} />
        <div className={`strength-bar ${strength.score >= 4 ? strength.className : ''}`} />
        <div className={`strength-bar ${strength.score >= 5 ? strength.className : ''}`} />
      </div>
      <div className={`strength-label ${strength.className} text-right`}>
        {strength.label}
      </div>
    </div>
  );
};
