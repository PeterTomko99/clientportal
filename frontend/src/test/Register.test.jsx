import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { vi } from 'vitest';
import Register from '../pages/Register';
import api from '../api';

vi.mock('../api');

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return { ...actual, useNavigate: () => mockNavigate };
});

describe('Register', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  function setup() {
    render(
      <MemoryRouter>
        <Register />
      </MemoryRouter>
    );
  }

  async function fillAndSubmit() {
    await userEvent.type(screen.getByLabelText(/full name/i), 'Jan Novak');
    await userEvent.type(screen.getByLabelText(/email/i), 'jan@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'password123');
    await userEvent.click(screen.getByRole('button', { name: /create account/i }));
  }

  it('renders all fields without a role selector', () => {
    setup();
    expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.queryByLabelText(/role/i)).not.toBeInTheDocument();
  });

  it('navigates to login with success message on successful registration', async () => {
    api.post.mockResolvedValue({});
    setup();
    await fillAndSubmit();

    await waitFor(() =>
      expect(mockNavigate).toHaveBeenCalledWith('/login', {
        state: { message: 'Account created. Please sign in.' },
      })
    );
  });

  it('shows inline error on 409 conflict', async () => {
    api.post.mockRejectedValue({ response: { status: 409 } });
    setup();
    await fillAndSubmit();

    await waitFor(() =>
      expect(screen.getByText('An account with that email already exists.')).toBeInTheDocument()
    );
  });

  it('clears error when user starts typing', async () => {
    api.post.mockRejectedValue({ response: { status: 409 } });
    setup();
    await fillAndSubmit();

    await waitFor(() =>
      expect(screen.getByText('An account with that email already exists.')).toBeInTheDocument()
    );

    await userEvent.type(screen.getByLabelText(/email/i), 'x');
    expect(screen.queryByText('An account with that email already exists.')).not.toBeInTheDocument();
  });
});
