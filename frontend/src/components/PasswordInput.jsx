import { useState, forwardRef } from 'react';
import { Eye, EyeOff, Lock, AlertCircle } from 'lucide-react';

export const PasswordInput = forwardRef(({ label, error, id, ...props }, ref) => {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <div className="field-group">
      {label && (
        <label htmlFor={id || props.name} className="field-label">
          {label}
        </label>
      )}
      <div className="field-wrapper">
        <div className="field-icon">
          <Lock size={18} strokeWidth={2.5} />
        </div>
        <input
          id={id || props.name}
          type={showPassword ? 'text' : 'password'}
          ref={ref}
          className={`field-input ${error ? 'has-error' : ''}`}
          {...props}
        />
        <button
          type="button"
          className="pwd-toggle"
          onClick={() => setShowPassword(!showPassword)}
          aria-label={showPassword ? 'Hide password' : 'Show password'}
        >
          {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
        </button>
      </div>
      {error && (
        <div className="field-error">
          <AlertCircle size={14} />
          <span>{error.message}</span>
        </div>
      )}
    </div>
  );
});

PasswordInput.displayName = 'PasswordInput';
