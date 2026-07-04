import { forwardRef } from 'react';
import { AlertCircle } from 'lucide-react';

export const InputField = forwardRef(({ label, icon: Icon, error, id, ...props }, ref) => {
  return (
    <div className="field-group">
      {label && (
        <label htmlFor={id || props.name} className="field-label">
          {label}
        </label>
      )}
      <div className="field-wrapper">
        {Icon && (
          <div className="field-icon">
            <Icon size={18} strokeWidth={2.5} />
          </div>
        )}
        <input
          id={id || props.name}
          ref={ref}
          className={`field-input ${!Icon ? 'no-icon' : ''} ${error ? 'has-error' : ''}`}
          {...props}
        />
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

InputField.displayName = 'InputField';
