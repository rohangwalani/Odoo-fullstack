import { useState, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { Mail, User, Phone, Building2, Upload } from 'lucide-react';
import { registerSchema } from '../utils/validators';
import { AuthLayout } from '../components/AuthLayout';
import { InputField } from '../components/InputField';
import { PasswordInput } from '../components/PasswordInput';
import { PasswordStrength } from '../components/PasswordStrength';
import { Button } from '../components/Button';
import axiosInstance from '../api/axiosInstance';
import toast from 'react-hot-toast';

export const RegisterPage = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const fileInputRef = useRef(null);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(registerSchema),
  });

  const passwordValue = watch('password', '');

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      // Mock API call
      // await axiosInstance.post('/auth/register', data);
      await new Promise((resolve) => setTimeout(resolve, 1500));
      
      toast.success('Account created successfully!', { className: 'custom-toast' });
      navigate('/login');
    } catch (error) {
      // handled by interceptor
    } finally {
      setLoading(false);
    }
  };

  const triggerFileUpload = () => {
    fileInputRef.current?.click();
  };

  return (
    <AuthLayout title="Create Account">
      <form onSubmit={handleSubmit(onSubmit)} className="flex-col gap-4">
        
        {/* Company Name with Upload Icon */}
        <div className="flex-row gap-2">
          <div style={{ flex: 1 }}>
            <InputField
              label="Company Name"
              placeholder="Enter company name"
              icon={Building2}
              error={errors.companyName}
              {...register('companyName')}
            />
          </div>
          <div className="field-group" style={{ width: 'auto' }}>
            <label className="field-label">Logo</label>
            <button 
              type="button" 
              onClick={triggerFileUpload}
              style={{
                height: '42px',
                padding: '0 0.75rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: 'var(--bg-input)',
                border: '1px solid rgba(155, 114, 170, 0.2)',
                borderRadius: 'var(--radius-md)',
                color: 'var(--accent-primary)',
                cursor: 'pointer'
              }}
              title="Upload Logo"
            >
              <Upload size={20} />
            </button>
            <input type="file" ref={fileInputRef} style={{ display: 'none' }} accept="image/*" />
          </div>
        </div>

        <InputField
          label="Full Name"
          placeholder="Enter your full name"
          icon={User}
          error={errors.fullName}
          {...register('fullName')}
        />
        <InputField
          label="Phone Number"
          type="tel"
          placeholder="Phone number"
          icon={Phone}
          error={errors.phoneNumber}
          {...register('phoneNumber')}
        />

        <InputField
          label="Email Address"
          type="email"
          placeholder="Enter your email"
          icon={Mail}
          error={errors.email}
          {...register('email')}
        />

        <div>
          <PasswordInput
            label="Create Password"
            placeholder="Enter a strong password"
            error={errors.password}
            {...register('password')}
          />
          <PasswordStrength password={passwordValue} />
        </div>

        <PasswordInput
          label="Confirm Password"
          placeholder="Confirm your password"
          error={errors.confirmPassword}
          {...register('confirmPassword')}
        />

        <Button type="submit" loading={loading} className="mt-2">
          Create Account
        </Button>

        <div className="text-center text-sm text-muted mt-2">
          Already have an account?{' '}
          <Link to="/login" className="auth-link">
            Sign In
          </Link>
        </div>
      </form>
    </AuthLayout>
  );
};
