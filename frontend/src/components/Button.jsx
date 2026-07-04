import { Spinner } from './Spinner';

export const Button = ({ 
  children, 
  variant = 'primary', 
  loading = false, 
  disabled, 
  type = 'button',
  className = '',
  ...props 
}) => {
  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={`btn btn-${variant} ${className}`}
      {...props}
    >
      {loading && <Spinner size="md" />}
      {children}
    </button>
  );
};
