import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { Mail } from 'lucide-react';
import { loginSchema } from '../utils/validators';
import { AuthLayout } from '../components/AuthLayout';
import { InputField } from '../components/InputField';
import { PasswordInput } from '../components/PasswordInput';
import { Button } from '../components/Button';
import axiosInstance from '../api/axiosInstance';
import toast from 'react-hot-toast';
import { useAuth } from '../hooks/useAuth';

export const LoginPage = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(loginSchema),
  });

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      const response = await axiosInstance.post('/auth/login', data);
      
      const { token, ...userData } = response.data;
      login(token, userData);
      toast.success('Successfully logged in!', { className: 'custom-toast' });
      navigate('/dashboard');
    } catch (error) {
      // Error handled by interceptor
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Welcome back" subtitle="Enter your details to access your account.">
      <form onSubmit={handleSubmit(onSubmit)} className="flex-col gap-4">
        <InputField
          label="Email Address"
          type="email"
          placeholder="Enter your email"
          icon={Mail}
          error={errors.email}
          {...register('email')}
        />

        <PasswordInput
          label="Password"
          placeholder="Enter your password"
          error={errors.password}
          {...register('password')}
        />

        <div className="flex-between">
          <label className="checkbox-group">
            <input type="checkbox" className="checkbox-input" {...register('rememberMe')} />
            <span className="checkbox-label">Remember me</span>
          </label>
          <Link to="/forgot-password" className="auth-link text-sm">
            Forgot password?
          </Link>
        </div>

        <Button type="submit" loading={loading} className="mt-2">
          Sign In
        </Button>

        <div className="text-center text-sm text-muted mt-4">
          Don't have an account?{' '}
          <Link to="/register" className="auth-link">
            Create account
          </Link>
        </div>
      </form>
    </AuthLayout>
  );
};
