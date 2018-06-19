#version 130

/* The position of the vertex as two-dimensional vector */
in vec2 vertex;

/* Write interpolated texture coordinate to fragment shader */
out vec2 texcoord;

void main(void) {
  gl_Position = vec4(vertex, 0.0, 1.0);
  texcoord = vertex;
}
