import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { vi } from 'vitest';
import Login from '../pages/Login';
import api from '../api';

vi.mock('../api');

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

  it('shows inline error message on 401', async () => {
    api.post.mockRejectedValue({ response: { status: 401 } });
    setup();
    await fillAndSubmit();

    await waitFor(() =>
      expect(screen.getByText('Invalid email or password.')).toBeInTheDocument()
    );
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('clears error when user starts typing again', async () => {
    api.post.mockRejectedValue({ response: { status: 401 } });
    setup();
    await fillAndSubmit();

    await waitFor(() =>
      expect(screen.getByText('Invalid email or password.')).toBeInTheDocument()
    );

    await userEvent.type(screen.getByLabelText(/email/i), 'x');
    expect(screen.queryByText('Invalid email or password.')).not.toBeInTheDocument();
  });

  it('does not store token on failed login', async () => {
    api.post.mockRejectedValue({ response: { status: 401 } });
    setup();
    await fillAndSubmit();

    await waitFor(() => expect(localStorage.getItem('token')).toBeNull());
  });
});
