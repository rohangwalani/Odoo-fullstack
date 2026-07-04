import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { Mail, ArrowLeft } from 'lucide-react';
import { forgotPasswordSchema } from '../utils/validators';
import { AuthLayout } from '../components/AuthLayout';
import { InputField } from '../components/InputField';
import { Button } from '../components/Button';
import axiosInstance from '../api/axiosInstance';
import toast from 'react-hot-toast';

export const ForgotPasswordPage = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(forgotPasswordSchema),
  });

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      // Mock API call
      // await axiosInstance.post('/auth/forgot-password', data);
      await new Promise((resolve) => setTimeout(resolve, 1500));
      
      toast.success('OTP sent to your email!', { className: 'custom-toast' });
      navigate('/verify-email', { state: { email: data.email } });
    } catch (error) {
      // handled
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Forgot Password" subtitle="Enter your email to receive an OTP.">
      <form onSubmit={handleSubmit(onSubmit)} className="flex-col gap-5">
        <InputField
          label="Email Address"
          type="email"
          placeholder="Enter your email"
          icon={Mail}
          error={errors.email}
          {...register('email')}
        />

        <Button type="submit" loading={loading} className="mt-2">
          Send OTP
        </Button>

        <div className="text-center mt-2">
          <Link to="/login" className="auth-link text-sm flex-row justify-center gap-2">
            <ArrowLeft size={16} />
            Back to Login
          </Link>
        </div>
      </form>
    </AuthLayout>
  );
};
