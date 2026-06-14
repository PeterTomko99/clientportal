import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { vi } from 'vitest';
import Login from '../pages/Login';
import api from '../api';

vi.mock('../api');
vi.mock('react-hot-toast', () => ({
  default: { success: vi.fn(), error: vi.fn() },
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return { ...actual, useNavigate: () => mockNavigate };
});

describe('Login', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  function setup() {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );
  }

  async function fillAndSubmit(email = 'test@example.com', password = 'password123') {
    await userEvent.type(screen.getByLabelText(/email/i), email);
    await userEvent.type(screen.getByLabelText(/password/i), password);
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));
  }

  it('stores token and navigates to dashboard on success', async () => {
    api.post.mockResolvedValue({ data: { token: 'test-jwt-token' } });
    setup();
    await fillAndSubmit();

    await waitFor(() => {
      expect(localStorage.getItem('token')).toBe('test-jwt-token');
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('shows invalid credentials error on 401', async () => {
    const toast = (await import('react-hot-toast')).default;
    api.post.mockRejectedValue({ response: { status: 401 } });
    setup();
    await fillAndSubmit();

    await waitFor(() =>
      expect(toast.error).toHaveBeenCalledWith('Invalid email or password.')
    );
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('does not store token on failed login', async () => {
    api.post.mockRejectedValue({ response: { status: 401 } });
    setup();
    await fillAndSubmit();

    await waitFor(() => expect(localStorage.getItem('token')).toBeNull());
  });
});
