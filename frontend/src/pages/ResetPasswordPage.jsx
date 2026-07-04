import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { resetPasswordSchema } from '../utils/validators';
import { AuthLayout } from '../components/AuthLayout';
import { PasswordInput } from '../components/PasswordInput';
import { PasswordStrength } from '../components/PasswordStrength';
import { Button } from '../components/Button';
import axiosInstance from '../api/axiosInstance';
import toast from 'react-hot-toast';

export const ResetPasswordPage = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(resetPasswordSchema),
  });

  const passwordValue = watch('password', '');

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      // Mock API call
      // await axiosInstance.post('/auth/reset-password', data);
      await new Promise((resolve) => setTimeout(resolve, 1500));
      
      toast.success('Password reset successfully! Please login.', { className: 'custom-toast' });
      navigate('/login');
    } catch (error) {
      // handled
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Reset Password" subtitle="Enter your new password below.">
      <form onSubmit={handleSubmit(onSubmit)} className="flex-col gap-5">
        <div>
          <PasswordInput
            label="New Password"
            placeholder="Enter new password"
            error={errors.password}
            {...register('password')}
          />
          <PasswordStrength password={passwordValue} />
        </div>

        <PasswordInput
          label="Confirm New Password"
          placeholder="Confirm new password"
          error={errors.confirmPassword}
          {...register('confirmPassword')}
        />

        <Button type="submit" loading={loading} className="mt-2">
          Reset Password
        </Button>
      </form>
    </AuthLayout>
  );
};
