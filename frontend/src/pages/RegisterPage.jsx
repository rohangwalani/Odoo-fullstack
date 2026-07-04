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
import toast from 'react-hot-toast';
import { useAuth } from '../hooks/useAuth';

export const RegisterPage = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  const { registerUser, user, login } = useAuth();

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
      await new Promise((resolve) => setTimeout(resolve, 1000));
      
      // Store in mock database
      const userData = {
        name: data.fullName,
        email: data.email,
        phone: data.phoneNumber,
        company: data.companyName,
        password: data.password, // Only storing for mock purposes
        role: user?.role === 'Admin' ? 'Employee' : 'Admin' // Admins create Employees, public users become Admins
      };
      
      const newUser = registerUser(userData);
      
      if (user?.role === 'Admin') {
        toast.success(`Email sent to ${data.email} with login credentials!`, { className: 'custom-toast' });
        navigate('/dashboard');
      } else {
        toast.success('Account created successfully!', { className: 'custom-toast' });
        login(newUser.email, newUser);
        navigate('/dashboard');
      }
    } catch (error) {
      toast.error(error.message || 'Registration failed');
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
          placeholder="Enter phone number"
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
